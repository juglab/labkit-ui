
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
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DenoiSegSegmenter implements Segmenter {

	private final Context context;

	private boolean trained = false;

	private boolean canceled = false;

	private DenoiSegTraining training;

	private DenoiSegParameters params;

	private File model;

	ModelZooArchive archive;

	@Parameter
	private LogService logService;

	private double threshold = 0.5;

	public DenoiSegSegmenter(Context context) {
		this.context = context;
		logService = context.getService(LogService.class);

		params = new DenoiSegParameters();
	}

	@Override
	public void editSettings(JFrame dialogParent,
		List<Pair<ImgPlus<?>, Labeling>> trainingData)
	{
		// TODO: warn users if not enough labeled data
		// TODO: this happens on the EDT, in case of large depth, this may cause slow downs
		int[] dims = Intervals.dimensionsAsIntArray(trainingData.get(0).getA());
		int nLabeled = countNumberLabeled(trainingData);

		dialogParent= new DenoiSegParametersDialog(params, dims, nLabeled);
		dialogParent.pack();
		dialogParent.setVisible(true);
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		// sanity check 1
		// TODO what if there are more than one pair? is the call to dimension proper (Axis?)?
		int dim = (int) trainingData.get(0).getA().dimension(2);

		// TODO: check if data is a movie, otherwise the labels will be 3D and will interfere with the training
		training = new DenoiSegTraining(context);

		training.addCallbackOnCancel(this::cancel);
		training.init(new DenoiSegConfig()
				.setNumEpochs(params.getNumEpochs())
				.setStepsPerEpoch(params.getNumStepsPerEpoch())
				.setBatchSize(params.getBatchSize())
				.setPatchShape(params.getPatchShape())
				.setNeighborhoodRadius(params.getNeighborhoodRadius()));

		// sanity checks 2
		List<Integer> labeledIndices = getLabeledIndices(trainingData);
		int nLabeled = labeledIndices.size();

		// TODO: replace with cancellation exception instead of asking the user?
		if (nLabeled == 0) {
			int r = showWarning("Not enough ground-truth labels, do you want to continue?");
			if (r != 0) {
				training.cancel();
				return;
			}
		} else if (params.getNumberValidation() > nLabeled - 5) { // TODO: 5 is arbitrary, replace by 0?
			int r = showWarning("Not enough training ground-truth labels, do you want to continue?");
			if (r != 0) {
				training.cancel();
				return;
			}
		} else if (params.getNumberValidation() < 5) {
			int r = showWarning("Not enough validation labels, do you want to continue?");
			if (r != 0) {
				training.cancel();
				return;
			}
		}

		// TODO maybe we need to randomize the order
		int nValidate = 0;
		List<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<IntType>>> validationData = new ArrayList<>(); // used by threshold optimizer
		for (int i = 0; i < dim; i++) {
			RandomAccessibleInterval x = Views.hyperSlice((RandomAccessibleInterval) trainingData.get(i).getA(), 2, i);
			RandomAccessibleInterval y = Views.hyperSlice((RandomAccessibleInterval<IntType>) trainingData.get(i).getB().getIndexImg(), 2, i);

			// if not labeled, DenoiSeg expect null value (not a "black" image)
			if (!labeledIndices.contains(i)) {
				y = null;
			}

			if (y != null && nValidate < params.getNumberValidation()) {
				training.input().addValidationData(x, y);
				validationData.add(new ValuePair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<IntType>>(x, y));
				nValidate++;
			} else {
				training.input().addTrainingData(x, y);
			}
		}

		if (training.getDialog() != null) training.getDialog().addTask("Prediction");

		training.train(); // method returns upon cancelling or finishing training

		trained = !training.isCanceled();
		if (!trained) {
			cancel();
		} else {
			try {
				model = training.output().exportBestTrainedModel();
				archive = openModel(model);

				ThresholdOptimizer thresholdOptimizer = new ThresholdOptimizer(context, archive, validationData);
					Map<Double, Double> results = thresholdOptimizer.run();

					if (results != null && !results.isEmpty()) {
						double max = -1;
						Iterator<Double> it = results.keySet().iterator();
						while (it.hasNext()) {
							double t = it.next();
							if (results.get(t) > max) {
								max = results.get(t);
								threshold = t;
							}
						}
					}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private int countNumberLabeled(List<Pair<ImgPlus<?>, Labeling>> trainingData){
		return getLabeledIndices(trainingData).size();
	}

	private List<Integer> getLabeledIndices(List<Pair<ImgPlus<?>, Labeling>> trainingData){
		List<Integer> list = new ArrayList<Integer>();
		for(Pair<ImgPlus<?>, Labeling> p: trainingData){
			int dim = (int) trainingData.get(0).getA().dimension(2);

			for(int i=0; i<dim; i++) {
				RandomAccessibleInterval label = Views.hyperSlice((RandomAccessibleInterval<IntType>) p.getB().getIndexImg(), 2, i);
				if (isLabeled(label)) {
					list.add(i);
				}
			}
		}
		return list;
	}

	// one label = image labeled...
	private <T extends RealType> boolean isLabeled(RandomAccessibleInterval<T> img) {
		Iterator<T> it = Views.iterable(img).iterator();

		while(it.hasNext()){
			float f = it.next().getRealFloat();

			if(f > 0){
				return true;
			}
		}

		return false;
	}

	public void cancel() {
		canceled = true;
		if(training != null) training.dispose();
	}

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation) {

		// TODO this realods the archive, it should probably keep it in memory? how likely are users to move the cursor through the stack and segment frame by frame?
		if (archive != null) {
			try {
				final DenoiSegPrediction prediction = new DenoiSegPrediction(context);
				prediction.setTrainedModel(archive);

				// extract interval on the image
				final RandomAccessibleInterval slice = Views.interval(image, outputSegmentation);

				// check whether single image or "movie"
				String axes;
				int[] dims = Intervals.dimensionsAsIntArray(slice);
				if(dims[2] > 1){
					axes = "XYB";
				} else {
					axes = "XY";
				}

				// predict
				final DenoiSegOutput<?, ?> res = prediction.predict(slice, axes);
				final RandomAccessibleInterval<FloatType> probabilityMaps = (RandomAccessibleInterval<FloatType>) res.getSegmented();

				// extract foreground prediction
				final RandomAccessibleInterval<FloatType> foregroundMap;
				if(axes.compareTo("XYB") == 0){ // XYBC with channel being the dimension along bg-fg-border
					foregroundMap = Views.hyperSlice(probabilityMaps, 3, 1);
				} else { // XYC
					// add one dimension
					foregroundMap = Views.addDimension(Views.hyperSlice(probabilityMaps, 2, 1), 0, 0);
				}

				// copy into outputSegmentation while thresholding
				LoopBuilder.setImages(outputSegmentation, foregroundMap).forEachPixel( (o,i) -> o.setInteger(i.get() > threshold ? 1: 0));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		if (archive != null) {
			try {
				final DenoiSegPrediction prediction = new DenoiSegPrediction(context);
				prediction.setTrainedModel(archive);

				// extract slice
				final RandomAccessibleInterval slice = Views.interval(image, Views.hyperSlice(outputProbabilityMap, 2, 0));

				// check whether single image or "movie"
				String axes;
				int[] dims = Intervals.dimensionsAsIntArray(image);
				if(dims[2] > 1){
					axes = "XYB";
				} else {
					axes = "XY";
				}

				// predict
				final DenoiSegOutput<?, ?> res = prediction.predict((RandomAccessibleInterval) image, axes);
				final RandomAccessibleInterval<FloatType> probabilityMaps = (RandomAccessibleInterval<FloatType>) res.getSegmented();

				// extract foreground prediction
				final RandomAccessibleInterval<FloatType> probabilityMap, backgroundMap, foregroundMap;
				if(axes.compareTo("XYB") == 0){ // XYBC with channel being the dimension along bg-fg-border
					backgroundMap = Views.hyperSlice(probabilityMaps, 3, 0);
					foregroundMap = Views.hyperSlice(probabilityMaps, 3, 1);
				} else { // XYC
					backgroundMap = Views.addDimension(Views.hyperSlice(probabilityMaps, 2, 0), 0, 0);
					foregroundMap = Views.addDimension(Views.hyperSlice(probabilityMaps, 2, 1), 0, 0);
				}
				probabilityMap = Views.stack(backgroundMap, foregroundMap);

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
		// TODO potential problems
		if (model != null) {
			try {
				// TODO should we make sure to name the model .io.zip to make it clear it is a zoomodel?
				Path newPath = Paths.get(path);
				Files.copy(model.toPath(), newPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// TODO no model to be saved
		}
	}

	@Override
	public void openModel(String path) {
		if(path.endsWith("bioimage.io.zip")) {
			model = new File(path);
			try {
				archive = openModel(model);
				trained = true;
			} catch (IOException e) {
				trained = false;
				e.printStackTrace();
			}
		} else {
			// TODO feedback to user
		}
	}

	private ModelZooArchive openModel(File model) throws IOException {
		ModelZooArchive archive = null;
		ModelZooService modelZooService = context.getService(ModelZooService.class);
		archive = modelZooService.io().open(model);

		return archive;
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("background", "foreground");
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		int[] dims = Intervals.dimensionsAsIntArray(image);

		/*for (int i=2; i<dims.length;i++) {
			dims[i] = 1;
		}*/
		return dims;
	}

	@Override
	public boolean requiresFixedCellSize() {
		return false;
	}

	public double getOptimizedThreshold(){
		return threshold;
	}

	// TODO is there already such a mechanism in labkit/imagej2?
 	private int showWarning(String message){
		int reply = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION);
		return reply;
	}

	public static void main(String... args) throws IOException, ExecutionException, InterruptedException {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		Object data = imageJ.io().open("/Users/deschamp/Downloads/denoiseg_mouse/Test_Labkit/Substack/Substack.tif");
		imageJ.ui().show(data);
		imageJ.command().run(LabkitPlugin.class, true);
	}
}
