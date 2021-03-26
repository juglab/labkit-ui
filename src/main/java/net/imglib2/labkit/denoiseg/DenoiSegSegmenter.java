
package net.imglib2.labkit.denoiseg;

import de.csbdresden.denoiseg.predict.DenoiSegOutput;
import de.csbdresden.denoiseg.predict.DenoiSegPrediction;
import de.csbdresden.denoiseg.threshold.ThresholdOptimizer;
import de.csbdresden.denoiseg.train.DenoiSegConfig;
import de.csbdresden.denoiseg.train.DenoiSegTraining;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.modelzoo.ModelZooArchive;
import net.imagej.modelzoo.ModelZooService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.plugin.LabkitPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.scijava.Context;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DenoiSegSegmenter implements Segmenter {

	final private Context context;

	private boolean trained = false;

	private DenoiSegTraining training;
	private DenoiSegParameters params;

	private File model;
	private ModelZooArchive archive;

	private double threshold = 0.5;

	public DenoiSegSegmenter(Context context) {
		this.context = context;

		params = new DenoiSegParameters();
	}

	@Override
	public void editSettings(JFrame dialogParent,
		List<Pair<ImgPlus<?>, Labeling>> trainingData)
	{
		int[] dims = Intervals.dimensionsAsIntArray(trainingData.get(0).getA()); // TODO: assume that dims are the same for every slice
		dialogParent= new DenoiSegParametersDialog(params, dims);
		dialogParent.pack();
		dialogParent.setVisible(true);
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		// Sanity check
		if(trainingData.size() != 1)
			throw new UnsupportedOperationException();

		// TODO: check if data is a movie XYT (as opposed to a stack), otherwise the labels will be 3D (each stroke yielding
		//  pixel labels on multiple frames) and will interfere with the training

		// Retrieve the only image/labeling pair
		RandomAccessibleInterval<?> image = (RandomAccessibleInterval) trainingData.get(0).getA();
		RandomAccessibleInterval<IntType> labeling = (RandomAccessibleInterval<IntType>) trainingData.get(0).getB().getIndexImg();

		// Check number of labeled slices
		final List<Integer> labeledImageIndices = getLabeledIndices(labeling);
		final int nLabeled = labeledImageIndices.size();
		final int nValidation = Math.max(1, Math.round(params.getValidationPercentage() * nLabeled / 100f) );
		if (nLabeled < 2) {
			showError("Not enough ground-truth labels (minimum of 2 labeled slices required).");
			return;
		}

		// Instantiate DenoiSeg training
		training = new DenoiSegTraining(context);
		training.addCallbackOnCancel(() -> {
			if(training != null) training.dispose();
		});
		training.init(new DenoiSegConfig()
				.setNumEpochs(params.getNumEpochs())
				.setStepsPerEpoch(params.getNumStepsPerEpoch())
				.setBatchSize(params.getBatchSize())
				.setPatchShape(params.getPatchShape())
				.setNeighborhoodRadius(params.getNeighborhoodRadius()));


		// Remember validation data for later use with the ThresholdOptimizer
		List<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<IntType>>> validationData = new ArrayList<>(nValidation);

		// Add training and validation data
		int nValidationCounter = 0;
		final int depth = Intervals.dimensionsAsIntArray(labeling)[2];
		for (int i = 0; i < depth; i++) {
			// Extract slice
			RandomAccessibleInterval x = Views.hyperSlice(image, 2, i);
			RandomAccessibleInterval y = Views.hyperSlice(labeling, 2, i);

			// If the slice is not labeled, DenoiSeg expect null value for the GT label (not a "black" image)
			if (!labeledImageIndices.contains(i)) {
				y = null;
			}

			if (y != null && nValidationCounter < nValidation) { // Validation y cannot be null
				training.input().addValidationData(x, y);
				validationData.add(new ValuePair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<IntType>>(x, y));
				nValidationCounter++;
			} else {
				training.input().addTrainingData(x, y);
			}
		}

		// Train model
		training.train(); // method returns upon cancelling or finishing training

		trained = !training.isCanceled();
		if (!trained) {
			if(training != null) training.dispose();
		} else {
			try {
				// Retrieve model and load it
				model = training.output().exportBestTrainedModel();
				archive = openModel(model);

				// Get optimised threshold
				threshold = optimizeThreshold(archive, validationData);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private double optimizeThreshold(ModelZooArchive archive, List<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<IntType>>> validationData) throws Exception {
		final ThresholdOptimizer thresholdOptimizer = new ThresholdOptimizer(context, archive, validationData);

		// Run threshold optimization, it returns a Map< threshold: metrics value >
		final Map<Double, Double> results = thresholdOptimizer.run();

		// Find threshold that maximizes the metrics
		return Collections.max(results.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
	}

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation) {

		// TODO is segment only called when trained == True? In which case remove clause
		if (trained && archive != null) {
			try {
				final DenoiSegPrediction prediction = new DenoiSegPrediction(context);
				prediction.setTrainedModel(archive);

				// Extract cell interval on the image
				final RandomAccessibleInterval cell = Views.interval(image, outputSegmentation);

				// Check whether single image or "movie"
				final String axes;
				if(Intervals.dimensionsAsIntArray(cell)[2] > 1){
					axes = "XYB";
				} else {
					axes = "XY";
				}

				// Predict
				final DenoiSegOutput<?, ?> res = prediction.predict(cell, axes);
				final RandomAccessibleInterval<FloatType> probabilityMaps = (RandomAccessibleInterval<FloatType>) res.getSegmented();

				// Extract foreground prediction
				final RandomAccessibleInterval<FloatType> foregroundMap;
				if(axes.compareTo("XYB") == 0){ // XYBC with channel dimension being the different predictions (background, foreground, border)
					foregroundMap = Views.hyperSlice(probabilityMaps, 3, 1);
				} else { // XYC
					// Add one dimension to match the output segmentation size
					foregroundMap = Views.addDimension(Views.hyperSlice(probabilityMaps, 2, 1), 0, 0);
				}

				// Threshold and copy to the output segmentation
				LoopBuilder.setImages(outputSegmentation, foregroundMap).forEachPixel( (o,i) -> o.setInteger(i.get() > threshold ? 1: 0));

				// TODO How to save theshold? In the name of the model? Show message to user? Separate JSON? Hijack modelzoo to put it in the .zip?

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		// TODO is predict only called when trained == True? In which case remove clause
		if (trained && archive != null) {
			try {
				final DenoiSegPrediction prediction = new DenoiSegPrediction(context);
				prediction.setTrainedModel(archive);

				// TODO Is predict called only on the whole stack?

				// Check whether single image or "movie"
				final String axes;
				if(Intervals.dimensionsAsIntArray(image)[2] > 1){
					axes = "XYB";
				} else {
					axes = "XY";
				}

				// Predict
				final DenoiSegOutput<?, ?> res = prediction.predict((RandomAccessibleInterval) image, axes);
				final RandomAccessibleInterval<FloatType> probabilityMaps = (RandomAccessibleInterval<FloatType>) res.getSegmented();

				// Extract predictions
				final RandomAccessibleInterval<FloatType> probabilityMap, backgroundMap, foregroundMap;
				if(axes.compareTo("XYB") == 0){ // XYBC with channel dimension being the different predictions (background, foreground, border)
					backgroundMap = Views.hyperSlice(probabilityMaps, 3, 0);
					foregroundMap = Views.hyperSlice(probabilityMaps, 3, 1);
				} else { // XYC
					// Add one dimension to match the output segmentation size
					backgroundMap = Views.addDimension(Views.hyperSlice(probabilityMaps, 2, 0), 0, 0);
					foregroundMap = Views.addDimension(Views.hyperSlice(probabilityMaps, 2, 1), 0, 0);
				}

				// Stack predictions
				probabilityMap = Views.stack(backgroundMap, foregroundMap);

				// Copy predictions to the output prediction
				LoopBuilder.setImages(outputProbabilityMap, probabilityMap).forEachPixel( (o,i) -> o.setReal(i.get()));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isTrained() {
		return trained;
	}

	@Override
	public void saveModel(String path) {
		if (archive != null) {
			try {
				// TODO this saves as .classifier, but should use bioimage.io.zip?
				// TODO save threshold in the name
				ModelZooService modelZooService = context.getService(ModelZooService.class);
				modelZooService.io().save(archive, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			showError("No model found.");
		}
	}

	@Override
	public void openModel(String path) {
		// TODO Should we try to load anyway and not just trust the extension?
		if(path.endsWith("bioimage.io.zip")) {
			model = new File(path);
			try {
				archive = openModel(model);
				trained = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			showError("Wrong extension (expected: bioimage.io.zip).");
		}
	}

	private ModelZooArchive openModel(File model) throws IOException {
		ModelZooArchive modelZooArchive = null;
		ModelZooService modelZooService = context.getService(ModelZooService.class);
		modelZooArchive = modelZooService.io().open(model);

		return modelZooArchive;
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("background", "foreground");
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		int[] dims = Intervals.dimensionsAsIntArray(image);

		// TODO try to explore thread-safe imagej-tensorflow/modelzoo to predict slice by slice

		return dims;
	}

	@Override
	public boolean requiresFixedCellSize() {
		return false;
	}

	public double getOptimizedThreshold(){
		return threshold;
	}

	private List<Integer> getLabeledIndices(RandomAccessibleInterval<IntType> labeling){
		List<Integer> list = new ArrayList<>();

		int dim = Intervals.dimensionsAsIntArray(labeling)[2];
		for(int i=0; i<dim; i++) {
			RandomAccessibleInterval<? extends IntegerType<?>> label = Views.hyperSlice(labeling, 2, i);
			if (isLabeled(label)) {
				list.add(i);
			}
		}
		return list;
	}

	private boolean isLabeled(RandomAccessibleInterval<? extends IntegerType<?>> img) {
		for(IntegerType<?> pixel : Views.iterable(img)){
			if(pixel.getInteger() > 0){
				return true;
			}
		}
		return false;
	}

 	private void showError(String message){
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String... args) throws IOException, ExecutionException, InterruptedException {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		Object data = imageJ.io().open("/Users/deschamp/Downloads/denoiseg_mouse/Test_Labkit/Stack/Stack.tif");
		imageJ.ui().show(data);
		imageJ.command().run(LabkitPlugin.class, true);
	}
}
