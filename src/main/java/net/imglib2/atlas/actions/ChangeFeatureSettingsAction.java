package net.imglib2.atlas.actions;

import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.trainable_segmention.gui.FeatureSettingsGui;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;

import java.util.Optional;

public class ChangeFeatureSettingsAction {

	private final MainFrame.Extensible extensible;
	private final Classifier classifier;

	public ChangeFeatureSettingsAction(MainFrame.Extensible extensible, Classifier classifier) {
		this.extensible = extensible;
		this.classifier = classifier;
		extensible.addAction("Change Feature Settings ...", "changeFeatures", this::action, "");
	}

	private void action() {
		Optional<FeatureSettings> fs = FeatureSettingsGui.show(extensible.context(), classifier.settings());
		if(!fs.isPresent())
			return;
		classifier.reset(fs.get(), extensible.getLabeling().getLabels());
	}
}
