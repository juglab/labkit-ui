
package net.imglib2.labkit.denoiseg;

import de.csbdresden.denoiseg.predict.DenoiSegPrediction;
import de.csbdresden.denoiseg.train.DenoiSegConfig;
import de.csbdresden.denoiseg.train.DenoiSegTraining;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.plugin.LabkitPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import org.scijava.Context;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DenoiSegSegmenter implements Segmenter {

	private final Context context;

	private int numEpochs = 1;//300;

	private int numStepsPerEpoch = 1;//200;

	private int batchSize = 64;

	private int patchShape = 16; // min=16, max=512, stepsize=16

	private int neighborhoodRadius = 5;

	private int numberValidation = 5;

	private boolean trained = false;

	//protected ModelZooArchive latestTrainedModel;

	//private ModelZooArchive bestTrainedModel;

	private boolean canceled = false;

	private DenoiSegTraining training;

	private DenoiSegPrediction prediction;

	private File model;

	public DenoiSegSegmenter(Context context) {
		this.context = context;
	}

	@Override
	public void editSettings(JFrame dialogParent,
		List<Pair<ImgPlus<?>, Labeling>> trainingData)
	{
		JPanel pane = new JPanel();
		if (!SwingUtilities.isEventDispatchThread()) {
			pane.add(new JLabel("Not EDT"));
		} else {
			pane.add(new JLabel("EDT alright"));
		}
		dialogParent = new JFrame();
		dialogParent.add(pane);
		dialogParent.pack();
		dialogParent.setVisible(true);

		/*
		What should be in the settings?
		- proportion train / val
		- number of epochs
		- number of steps per epoch
		- batch size
		- patch shape?
		- neighborhood radius
		 */
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		// sanity check 1

		// intervals

		int dim = (int) trainingData.get(0).getA().dimension(2); // TODO is call to dimension=2 correct?
		// TODO: check if data is a movie, otherwise the labels will be 3D and will interfere with the traning

		DenoiSegTraining training = new DenoiSegTraining(context);

		training.addCallbackOnCancel(this::cancel);
		training.init(new DenoiSegConfig()
				.setNumEpochs(numEpochs)
				.setStepsPerEpoch(numStepsPerEpoch)
				.setBatchSize(batchSize)
				.setPatchShape(patchShape)
				.setNeighborhoodRadius(neighborhoodRadius));

		// sanity checks 2
		int nLabeled = countNumberLabeled(trainingData); // TODO return list instead of number
		// TODO: replace with cancellation exception instead of asking the user?
		if(nLabeled == 0) {
			int r = showWarning("Not enough ground-truth labels, do you want to continue?");
			if(r != 0){
				training.cancel();
				return;
			}
		} else if(numberValidation > nLabeled-5){ // TODO: 5 is arbitrary, replace by 0?
			int r = showWarning("Not enough training ground-truth labels, do you want to continue?");
			if(r != 0){
				training.cancel();
				return;
			}
		} else if(numberValidation < 5){
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
			if(!isLabeled(y)){
				y = null;
			}

			// TODO check if they are automatically normalised, especially the labels
			if(y != null && nValidate < numberValidation){
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
		}

		// save the best model and return the File
		/*try {
			model = training.output().exportBestTrainedModel();
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		trained = true;

		System.out.println("Done");
	}

	private int showWarning(String message){
		int reply = JOptionPane.showConfirmDialog(null, message, "Warning", JOptionPane.YES_NO_OPTION);
		return reply;
	}

	private int countNumberLabeled(List<Pair<ImgPlus<?>, Labeling>> trainingData){
		int n = 0;
		for(Pair<ImgPlus<?>, Labeling> p: trainingData){
			int dim = (int) trainingData.get(0).getA().dimension(2);

			for(int i=0; i<dim; i++) {
				RandomAccessibleInterval label = Views.hyperSlice((RandomAccessibleInterval<IntType>) p.getB().getIndexImg(), 2, i);
				if (isLabeled(label)) {
					n++;
				}
			}
		}
		return n;
	}

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
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
	{
		System.out.println("Call to segment method");
	}

	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		System.out.println("Call to predict method");
/*
		prediction = new DenoiSegPrediction(context);
		prediction.setTrainedModel(latestTrainedModel);
		DenoiSegOutput<?, ?> res = prediction.predict(this.predictionInput, axes);
		this.denoised = datasetService.create(res.getDenoised());
		this.segmented = datasetService.create(res.getSegmented());
*/
	}

	@Override
	public boolean isTrained() {
		return trained;
	}

	@Override
	public void saveModel(String path) {
		// TODO check if trained and if the model is not null, then save
/*
		try {
			File model = training.output().exportBestTrainedModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
 */
	}

	@Override
	public void openModel(String path) {
		// TODO model zoo loader
	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("None");
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		// TODO return size of images

		return Intervals.dimensionsAsIntArray(image);
	}

	@Override
	public boolean requiresFixedCellSize() {
		return false;
	}

	public static void main(String... args) throws IOException {
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		Object data = imageJ.io().open("/Users/deschamp/Downloads/denoiseg_mouse/Test_Labkit/Stack.tif");
		imageJ.ui().show(data);
		imageJ.command().run(LabkitPlugin.class, true);
	}
}
