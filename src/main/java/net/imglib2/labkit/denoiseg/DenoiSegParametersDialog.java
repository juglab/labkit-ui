package net.imglib2.labkit.denoiseg;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DenoiSegParametersDialog extends JFrame {

    public DenoiSegParametersDialog(final DenoiSegParameters params, int[] dims){
        initComponents(params, dims);
    }

    private void initComponents(final DenoiSegParameters params, int[] dims) {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        JPanel paramPanel = getParamPanel(params, dims);
        JPanel okPanel = getLowerPanel();
        contentPane.add(paramPanel);
        contentPane.add(okPanel);

        this.setContentPane(contentPane);
    }

    private JPanel getParamPanel(final DenoiSegParameters params, int[] dims){
        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        int depth = dims[2];
        int minHW = Math.min(dims[0], dims[1]);

        // components
        JLabel nEpochsLabel = new JLabel("Number of epochs:");
        JLabel nStepsLabel = new JLabel("Number of steps / epoch:");
        JLabel batchSizeLabel =  new JLabel("Batch size:");
        JLabel patchShapeLabel = new JLabel("Patch shape:");
        JLabel neighborRadiusLabel = new JLabel("Neighborhood radius:");
        JLabel nValidationLabel = new JLabel("Labeled image % used for validation:");

        SpinnerModel epochModel = new SpinnerNumberModel(params.getNumEpochs(), 1, 10000, 1);
        JSpinner epochsSpinner = new JSpinner(epochModel);
        epochsSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) epochsSpinner.getValue();
                params.setNumEpochs(val);
            }
        });

        SpinnerModel stepsModel = new SpinnerNumberModel(params.getNumStepsPerEpoch(), 1, 10000, 1);
        JSpinner stepsSpinner = new JSpinner(stepsModel);
        stepsSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) stepsSpinner.getValue();
                params.setNumStepsPerEpoch(val);
            }
        });

        SpinnerModel batchModel = new SpinnerNumberModel(Math.min(64, params.getBatchSize()), 1, depth, 1);
        JSpinner batchSpinner = new JSpinner(batchModel);
        batchSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) batchSpinner.getValue();
                params.setBatchSize(val);
            }
        });

        SpinnerModel patchShapeModel = new SpinnerNumberModel(params.getPatchShape(), 16, 512, 16);
        JSpinner patchShapeSpinner = new JSpinner(patchShapeModel);
        patchShapeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) patchShapeSpinner.getValue();
                params.setPatchShape(val);
            }
        });

        // TODO: make sure this is correct
        SpinnerModel neighborModel = new SpinnerNumberModel(params.getNeighborhoodRadius(), 1, minHW, 1);
        JSpinner neighborSpinner = new JSpinner(neighborModel);
        neighborSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) neighborSpinner.getValue();
                params.setNeighborhoodRadius(val);
            }
        });

        SpinnerModel valModel = new SpinnerNumberModel(params.getValidationPercentage(), 1, 100, 1);
        JSpinner valSpinner = new JSpinner(valModel);
        valSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) valSpinner.getValue();
                params.setValidationPercentage(val);
            }
        });

        // lay out components
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 7, 3, 7);
        c.gridy = 0;
        c.gridx = 0;
        paramPane.add(nEpochsLabel, c);
        c.gridx = 1;
        paramPane.add(epochsSpinner, c);

        c.gridy++;
        c.gridx = 0;
        paramPane.add(nStepsLabel, c);
        c.gridx = 1;
        paramPane.add(stepsSpinner, c);

        c.gridy++;
        c.gridx = 0;
        paramPane.add(batchSizeLabel, c);
        c.gridx = 1;
        paramPane.add(batchSpinner, c);

        c.gridy++;
        c.gridx = 0;
        paramPane.add(patchShapeLabel, c);
        c.gridx = 1;
        paramPane.add(patchShapeSpinner, c);

        c.gridy++;
        c.gridx = 0;
        paramPane.add(neighborRadiusLabel, c);
        c.gridx = 1;
        paramPane.add(neighborSpinner, c);

        c.gridy++;
        c.gridx = 0;
        paramPane.add(nValidationLabel, c);
        c.gridx = 1;
        paramPane.add(valSpinner, c);

        return paramPane;
    }

    private JPanel getLowerPanel(){
        JPanel okPane = new JPanel();
        okPane.setLayout(new BoxLayout(okPane, BoxLayout.PAGE_AXIS));
        JButton okButton = new JButton("Done");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disposeWindow();
            }
        });
        okPane.add(okButton);

        return okPane;
    }

    private void disposeWindow(){
        this.dispose();
    }
}
