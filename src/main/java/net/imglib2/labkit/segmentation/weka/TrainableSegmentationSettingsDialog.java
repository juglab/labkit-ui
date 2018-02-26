package net.imglib2.labkit.segmentation.weka;

import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.trainable_segmention.gui.FeatureSettingsGui;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import weka.classifiers.Classifier;
import weka.gui.GenericObjectEditor;

import javax.swing.*;
import java.awt.*;

class TrainableSegmentationSettingsDialog
{

	private final Context context;

	private final JFrame dialogParent;

	private Classifier wekaClassifier;

	private FeatureSettings featureSettings;

	boolean okClicked = false;

	public TrainableSegmentationSettingsDialog( Context context, JFrame dialogParent, Classifier defaultWekaClassifier, FeatureSettings defaultFeatureSettings )
	{
		this.context = context;
		this.dialogParent = dialogParent;
		this.wekaClassifier = defaultWekaClassifier;
		this.featureSettings = defaultFeatureSettings;
	}

	public static void main(String... args) {
		Classifier  defaultWekaClassifier = new FastRandomForest();
		FeatureSettings defaultFeatureSettings = new FeatureSettings( GlobalSettings.default2dSettings(), GroupedFeatures.gauss() );
		TrainableSegmentationSettingsDialog dialog = new TrainableSegmentationSettingsDialog(new Context(), null, defaultWekaClassifier, defaultFeatureSettings);
		dialog.show();
		if(dialog.okClicked()) {
			dialog.featureSettings().features().forEach( setting -> System.out.println( setting.getName() ) );
			System.out.println( dialog.wekaClassifier() );
		}
	}

	public Classifier wekaClassifier()
	{
		return wekaClassifier;
	}

	public FeatureSettings featureSettings()
	{
		return featureSettings;
	}

	public boolean okClicked()
	{
		return okClicked;
	}

	public void show()
	{
		WekaClassifierPanel wekaPanel = new WekaClassifierPanel( wekaClassifier );
		FeatureSettingsGui featurePanel = new FeatureSettingsGui( context, featureSettings );
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Classification Algorithm", wekaPanel.getComponent());
		tabs.addTab("Features", addFrame("", featurePanel.getComponent()));
		okClicked = showResizeableOkCancelDialog( "Weka Trainable Segmentation Settings", addFrame("insets 0", tabs) );
		if(okClicked) {
			featureSettings = featurePanel.get();
			wekaClassifier = wekaPanel.get();
		}
	}

	private JComponent addFrame( String contraints, JComponent component )
	{
		JPanel panel = new JPanel();
		panel.setLayout( new MigLayout(contraints, "[grow]", "[grow]") );
		panel.add(component, "grow");
		return panel;
	}


	private boolean showResizeableOkCancelDialog(String title, Component content) {
		JDialog dialog = new JDialog(dialogParent, title, true);
		JOptionPane optionPane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		dialog.setContentPane(optionPane);
		dialog.setResizable(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		optionPane.addPropertyChangeListener( e -> {
			String prop = e.getPropertyName();
			if (dialog.isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop)))
				dialog.dispose();
		});
		dialog.pack();
		dialog.setVisible(true);
		return optionPane.getValue().equals(JOptionPane.OK_OPTION);
	}

	private static class WekaClassifierPanel {

		private final JPanel panel = new JPanel();

		private final JCheckBox checkBox;

		private final GenericObjectEditor editor;

		WekaClassifierPanel(Classifier initialValue ) {
			checkBox = new JCheckBox("FastRandomForest");
			editor = new GenericObjectEditor();
			editor.setClassType(Classifier.class);
			if( initialValue instanceof FastRandomForest )
				checkBox.setSelected(true);
			else
				editor.setValue( initialValue );
			panel.setLayout(new MigLayout());
			panel.add( checkBox, "wrap");
			panel.add( editor.getCustomPanel());
		}

		public JComponent getComponent() {
			return panel;
		}

		public Classifier get() {
			return checkBox.isSelected() ? new FastRandomForest() : (Classifier) editor.getValue();
		}
	}

}
