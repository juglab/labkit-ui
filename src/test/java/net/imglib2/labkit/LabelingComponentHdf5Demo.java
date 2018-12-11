
package net.imglib2.labkit;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.img.Img;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.type.numeric.NumericType;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * This example because it uses a multiview big data viewer dataset. But I
 * consider Labkit to work only on a dataset with one view.
 */
public class LabelingComponentHdf5Demo {

	public static void main(String... args) throws SpimDataException {
		JFrame frame = initFrame();
		final String fn = "http://fly.mpi-cbg.de/~pietzsch/bdv-examples/remote.xml";
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load(fn);
		frame.add(initLabelingComponent(frame, spimData));
		frame.setVisible(true);
	}

	private static JFrame initFrame() {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		return frame;
	}

	private static JComponent initLabelingComponent(JFrame frame,
		SpimDataMinimal spimData)
	{
		ImageLabelingModel model = initModel(spimData);
		BasicLabelingComponent labelingComponent = new BasicLabelingComponent(frame,
			model);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				labelingComponent.close();
			}
		});
		return labelingComponent.getComponent();
	}

	private static ImageLabelingModel initModel(SpimDataMinimal spimData) {
		// TODO simplify the creation of an ImageLabelingModel
		BdvShowable wrap = BdvShowable.wrap(spimData);
		Labeling labeling = Labeling.createEmpty(Arrays.asList("fg", "bg"), wrap
			.interval());
		boolean isTimeSeries = false;
		ImageLabelingModel result = new ImageLabelingModel(isTimeSeries);
		result.showable().set(wrap);
		result.labeling().set(labeling);
		return result;
	}

}
