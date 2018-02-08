package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.classification.Segmenter;
import net.imglib2.trainable_segmention.gui.FeatureSettingsGui;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;

import java.util.Optional;

public class ChangeFeatureSettingsAction {

	private final Extensible extensible;
	private final Segmenter segmenter;

	public ChangeFeatureSettingsAction(Extensible extensible, Segmenter segmenter ) {
		this.extensible = extensible;
		this.segmenter = segmenter;
		extensible.addAction("Change Feature Settings ...", "changeFeatures", this::action, "");
	}

	private void action() {
		Optional<FeatureSettings> fs = FeatureSettingsGui.show(extensible.context(), segmenter.settings());
		if(!fs.isPresent())
			return;
		segmenter.reset(fs.get(), null);
	}
}
