package net.imglib2.labkit.denoiseg;

public class DenoiSegParameters {

    protected int numEpochs = 1;//300;

    protected int numStepsPerEpoch = 1;//200;

    protected int batchSize = 64;

    protected int patchShape = 16; // min=16, max=512, stepsize=16

    protected int neighborhoodRadius = 5;

    protected int numberValidation = 5;

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("DenoiSeg parameters\n");
        sb.append("-------------------\n");
        sb.append("N epochs: "+numEpochs+"\n");
        sb.append("N steps per epoch: "+numStepsPerEpoch+"\n");
        sb.append("Batch size: "+batchSize+"\n");
        sb.append("Patch shape: "+patchShape+"\n");
        sb.append("Neighborhood radius: "+neighborhoodRadius+"\n");
        sb.append("Number of validation: "+numberValidation+"\n");

        return sb.toString();
    }
}
