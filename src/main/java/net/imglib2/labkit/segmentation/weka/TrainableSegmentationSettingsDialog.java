
package net.imglib2.labkit.segmentation.weka;

import net.imglib2.trainable_segmention.gui.FeatureSettingsGui;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;

class TrainableSegmentationSettingsDialog {

	private final Context context;

	private final JFrame dialogParent;

	private boolean useGpu;

	private FeatureSettings featureSettings;

	private boolean okClicked = false;

	public TrainableSegmentationSettingsDialog(Context context,
		JFrame dialogParent, boolean useGpu,
		FeatureSettings defaultFeatureSettings)
	{
		this.context = context;
		this.dialogParent = dialogParent;
		this.useGpu = useGpu;
		this.featureSettings = defaultFeatureSettings;
	}

	public static void main(String... args) {
		FeatureSettings defaultFeatureSettings = new FeatureSettings(GlobalSettings
			.default2d().build(), GroupedFeatures.gauss());
		TrainableSegmentationSettingsDialog dialog =
			new TrainableSegmentationSettingsDialog(new Context(), null,
				true, defaultFeatureSettings);
		dialog.show();
		if (dialog.okClicked()) {
			dialog.featureSettings().features().forEach(setting -> System.out.println(
				setting.getName()));
			System.out.println("Use Gpu: " + dialog.useGpu());
		}
	}

	public boolean useGpu() {
		return useGpu;
	}

	public FeatureSettings featureSettings() {
		return featureSettings;
	}

	public boolean okClicked() {
		return okClicked;
	}

	public void show() {
		JPanel dialogContent = new JPanel();
		dialogContent.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[][grow]"));
		JCheckBox gpuCheckBox = initUseGpuCheckBox(dialogContent);
		dialogContent.add(new FeatureSettingsGui(context, featureSettings).getComponent(), "grow");
		okClicked = showResizeableOkCancelDialog("Weka Trainable Segmentation Settings", dialogContent);
		if (okClicked) {
			featureSettings = new FeatureSettingsGui(context, featureSettings).get();
			useGpu = gpuCheckBox.isSelected();
		}
	}

	private JCheckBox initUseGpuCheckBox(JPanel dialogContent) {
		JCheckBox gpuCheckBox = new JCheckBox(
			"(experimental, should run on NVIDIA GPUs with 2 GB memory)");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new MigLayout("insets 0 0 10px 0", "[]20px[grow]"));
		topPanel.add(new JLabel("Use GPU acceleration:"));
		topPanel.add(gpuCheckBox);
		gpuCheckBox.setSelected(useGpu);
		dialogContent.add(topPanel, "grow, wrap");
		return gpuCheckBox;
	}

	private boolean showResizeableOkCancelDialog(String title,
		Component content)
	{
		JDialog dialog = new JDialog(dialogParent, title, true);
		JOptionPane optionPane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE,
			JOptionPane.OK_CANCEL_OPTION);
		dialog.setContentPane(optionPane);
		dialog.setResizable(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		optionPane.addPropertyChangeListener(e -> {
			String prop = e.getPropertyName();
			if (dialog.isVisible() && (e.getSource() == optionPane) &&
				(JOptionPane.VALUE_PROPERTY.equals(prop))) dialog.dispose();
		});
		dialog.pack();
		dialog.setVisible(true);
		return optionPane.getValue().equals(JOptionPane.OK_OPTION);
	}
}
