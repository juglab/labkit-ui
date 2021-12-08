
package net.imglib2.labkit.segmentation.weka;

import sc.fiji.labkit.pixel_classification.gui.FeatureSettingsUI;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.GroupedFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;

/**
 * Setting dialog for the Labkit Pixel Classification.
 */
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
		FeatureSettingsUI featureSettingsUI = new FeatureSettingsUI(context, featureSettings);
		dialogContent.add(featureSettingsUI, "grow");
		okClicked = showResizeableOkCancelDialog("Pixel Classification Settings", dialogContent);
		if (okClicked) {
			featureSettings = featureSettingsUI.get();
			useGpu = gpuCheckBox.isSelected();
		}
	}

	private JCheckBox initUseGpuCheckBox(JPanel dialogContent) {
		JCheckBox gpuCheckBox = new JCheckBox(
			"(experimental, requires CLIJ2 and NVIDIA GPU)");
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
