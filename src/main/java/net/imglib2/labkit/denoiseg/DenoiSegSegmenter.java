
package net.imglib2.labkit.denoiseg;

import de.csbdresden.denoiseg.predict.DenoiSegOutput;
import de.csbdresden.denoiseg.predict.DenoiSegPrediction;
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
import net.imglib2.view.Views;
import org.scijava.Context;

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

	public DenoiSegSegmenter(Context context) {
		this.context = context;

		params = new DenoiSegParameters();
	}

	@Override
	public void editSettings(JFrame dialogParent,
		List<Pair<ImgPlus<?>, Labeling>> trainingData)
	{
		// TODO: exception thrown if not enough labeled data, put safe-guards
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
		if(nLabeled == 0) {
			int r = showWarning("Not enough ground-truth labels, do you want to continue?");
			if(r != 0){
				training.cancel();
				return;
			}
		} else if(params.getNumberValidation() > nLabeled-5){ // TODO: 5 is arbitrary, replace by 0?
			int r = showWarning("Not enough training ground-truth labels, do you want to continue?");
			if(r != 0){
				training.cancel();
				return;
			}
		} else if(params.getNumberValidation() < 5){
			int r = showWarning("Not enough validation labels, do you want to continue?");
			if(r != 0){
				training.cancel();
				return;
			}
		}

		// TODO maybe we need to randomize the order
		int nValidate = 0;
		for(int i=0;i<dim;i++){
			RandomAccessibleInterval x = Views.hyperSlice((RandomAccessibleInterval) trainingData.get(i).getA(), 2 ,i );
			RandomAccessibleInterval y = Views.hyperSlice((RandomAccessibleInterval<IntType>) trainingData.get(i).getB().getIndexImg(), 2, i);

			// if not labeled, DenoiSeg expect null value (not a "black" image)
			if(!labeledIndices.contains(i)){
				y = null;
			}

			if(y != null && nValidate < params.getNumberValidation()){
				training.input().addValidationData(x,y);
				nValidate++;
			} else {
				training.input().addTrainingData(x,y);
			}
		}

		if(training.getDialog() != null) training.getDialog().addTask( "Prediction" );

		training.train(); // method returns upon cancelling or finishing training

		trained = !training.isCanceled();
		if(!trained) {
			System.out.println("canceled");
			cancel();
		} else {
			try {
				model = training.output().exportBestTrainedModel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Done");
	}

	private int showWarning(String message){
		int reply = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION);
		return reply;
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
		if (training != null) {
			ModelZooService modelZooService = context.getService(ModelZooService.class);
			try {
				ModelZooArchive archive = modelZooService.io().open(model);

				final DenoiSegPrediction prediction = new DenoiSegPrediction(context);
				prediction.setTrainedModel(archive);

				// check whether single image or "movie"
				String axes;
				int[] dims = Intervals.dimensionsAsIntArray(image);
				if(dims[2] > 1){
					axes = "XY";
				} else {
					axes = "XYB";
				}

				// predict
				final DenoiSegOutput<?, ?> res = prediction.predict((RandomAccessibleInterval) image, axes);
				final RandomAccessibleInterval<FloatType> probabilityMaps = (RandomAccessibleInterval<FloatType>) res.getSegmented();

				// extract foreground prediction
				final RandomAccessibleInterval<FloatType> foregroundMap;
				if(axes.compareTo("XYB") == 0){ // XYBC with channel being the dimension along bg-fg-border
					foregroundMap = (RandomAccessibleInterval<FloatType>) Views.hyperSlice(probabilityMaps, 3, 1);
				} else { // XYC
					foregroundMap = (RandomAccessibleInterval<FloatType>) Views.hyperSlice(probabilityMaps, 2, 1);
				}

				// copy into outputSegmentation while thresholding
				LoopBuilder.setImages(outputSegmentation, foregroundMap).forEachPixel( (o,i) -> o.setInteger(i.get() > 0.5 ? 1: 0));

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		if (training != null) {
			ModelZooService modelZooService = context.getService(ModelZooService.class);
			try {
				ModelZooArchive archive = modelZooService.io().open(model);

				final DenoiSegPrediction prediction = new DenoiSegPrediction(context);
				prediction.setTrainedModel(archive);

				// check whether single image or "movie"
				String axes;
				int[] dims = Intervals.dimensionsAsIntArray(image);
				if(dims[2] > 1){
					axes = "XY";
				} else {
					axes = "XYB";
				}

				// predict
				final DenoiSegOutput<?, ?> res = prediction.predict((RandomAccessibleInterval) image, axes);
				final RandomAccessibleInterval<FloatType> probabilityMaps = (RandomAccessibleInterval<FloatType>) res.getSegmented();

				// extract foreground prediction
				final RandomAccessibleInterval<FloatType> foregroundMap;
				if(axes.compareTo("XYB") == 0){ // XYBC with channel being the dimension along bg-fg-border
					foregroundMap = (RandomAccessibleInterval<FloatType>) Views.hyperSlice(probabilityMaps, 3, 1);
				} else { // XYC
					foregroundMap = (RandomAccessibleInterval<FloatType>) Views.hyperSlice(probabilityMaps, 2, 1);
				}

				LoopBuilder.setImages(outputProbabilityMap, foregroundMap).forEachPixel( (o,i) -> o.setReal(i.get()));

			} catch (IOException e) {
				e.printStackTrace();
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
		// currently no safety checks
		model = new File(path);
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("None");
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		int[] dims = Intervals.dimensionsAsIntArray(image);

		// TODO if prediction takes too long, reduce number of planes

		return dims;
	}

	@Override
	public boolean requiresFixedCellSize() {
		return false;
	}

	public static void main(String... args) throws IOException, ExecutionException, InterruptedException {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		Object data = imageJ.io().open("/Users/deschamp/Downloads/denoiseg_mouse/Test_Labkit/Stack.tif");
		imageJ.ui().show(data);
		imageJ.command().run(LabkitPlugin.class, true);
	}
}
