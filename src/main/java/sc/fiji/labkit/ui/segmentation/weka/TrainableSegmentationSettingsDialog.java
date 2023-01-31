/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.segmentation.weka;

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
