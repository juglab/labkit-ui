
package net.imglib2.labkit.actions;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.IntegerType;

import java.io.IOException;

/**
 * Implements the menu items for open, and saving the labeling, as well as ex
 * 
 * @author Matthias Arzt
 */
public class LabelingIoAction extends AbstractFileIoAction {

	private final LabelingModel labelingModel;
	private final LabelingSerializer serializer;

	public LabelingIoAction(Extensible extensible,
		LabelingModel labelingModel)
	{
		super(extensible, AbstractFileIoAction.LABELING_FILTER,
			AbstractFileIoAction.TIFF_FILTER);
		this.labelingModel = labelingModel;
		serializer = new LabelingSerializer(extensible.context());
		initSaveAction(MenuBar.LABELING_MENU, "Save Labeling ...", 2,
			new Action<Void>()
			{

				@Override
				public String suggestedFile() {
					return LabelingIoAction.this.labelingModel.defaultFileName();
				}

				@Override
				public void run(Void ignore, String filename) throws Exception {
					serializer.save(labelingModel.labeling().get(), filename);
				}
			}, "ctrl S");
		initOpenAction(MenuBar.LABELING_MENU, "Open Labeling ...", 1, this::open,
			"ctrl O");
		extensible.addMenuItem(MenuBar.LABELING_MENU, "Show Labeling in ImageJ", 3,
			ignore -> showLabelingInImageJ(), null, "");
	}

	private void showLabelingInImageJ() {
		RandomAccessibleInterval<? extends IntegerType<?>> img = labelingModel
			.labeling().get().getIndexImg();
		ImageJFunctions.show(LabkitUtils.uncheckedCast(img), "Labeling");
	}

	private void open(Void ignore, String filename) throws IOException {
		Labeling labeling = serializer.open(filename);
		labelingModel.labeling().set(labeling);
	}
}
