package net.imglib2.labkit.denoiseg;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DenoiSegParametersDialog extends JFrame {

    public DenoiSegParametersDialog(final DenoiSegParameters params, int[] dims, int nLabeled){
        initComponents(params, dims, nLabeled);
    }

    private void initComponents(final DenoiSegParameters params, int[] dims, int nLabeled) {
        // TODO: implement feedback to user on their choices of nValidation

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        JPanel paramPanel = getParamPanel(params, dims, nLabeled);
        JPanel okPanel = getLowerPanel();
        contentPane.add(paramPanel);
        contentPane.add(okPanel);

        this.setContentPane(contentPane);
    }

    private JPanel getParamPanel(final DenoiSegParameters params, int[] dims, int nLabeled){
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
        JLabel nValidationLabel = new JLabel("Number of validation labels:");

        SpinnerModel epochModel = new SpinnerNumberModel(300, 1, 10000, 1);
        JSpinner epochsSpinner = new JSpinner(epochModel);
        epochsSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) epochsSpinner.getValue();
                params.numEpochs = val;
            }
        });

        SpinnerModel stepsModel = new SpinnerNumberModel(200, 1, 10000, 1);
        JSpinner stepsSpinner = new JSpinner(stepsModel);
        stepsSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) stepsSpinner.getValue();
                params.numStepsPerEpoch = val;
            }
        });

        SpinnerModel batchModel = new SpinnerNumberModel(64, 1, depth, 1);
        JSpinner batchSpinner = new JSpinner(batchModel);
        batchSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) batchSpinner.getValue();
                params.batchSize = val;
            }
        });

        SpinnerModel patchShapeModel = new SpinnerNumberModel(16, 16, 512, 16);
        JSpinner patchShapeSpinner = new JSpinner(patchShapeModel);
        patchShapeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) patchShapeSpinner.getValue();
                params.patchShape = val;
            }
        });

        // TODO: make sure this is correct
        SpinnerModel neighborModel = new SpinnerNumberModel(5, 1, minHW, 1);
        JSpinner neighborSpinner = new JSpinner(neighborModel);
        neighborSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) neighborSpinner.getValue();
                params.neighborhoodRadius = val;
            }
        });

        // TODO: if nLabel too small, paint red and put another max than the default value
        SpinnerModel valModel = new SpinnerNumberModel(5, 1, nLabeled, 1);
        JSpinner valSpinner = new JSpinner(valModel);
        valSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = (int) valSpinner.getValue();
                params.numberValidation = val;
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
