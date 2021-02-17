package net.imglib2.labkit.denoiseg;

public class DenoiSegParameters {

    private int numEpochs = 300;

    private int numStepsPerEpoch = 200;

    private int batchSize = 64;

    private int patchShape = 16; // min=16, max=512, stepsize=16

    private int neighborhoodRadius = 5;

    private int numberValidation = 5;

    public int getNumEpochs(){
        return numEpochs;
    }

    public void setNumEpochs(int numEpochs){
        if(numEpochs > 0) this.numEpochs = numEpochs;
    }

    public int getNumStepsPerEpoch(){
        return numStepsPerEpoch;
    }

    public void setNumStepsPerEpoch(int numStepsPerEpoch){
        if(numStepsPerEpoch > 0) this.numStepsPerEpoch = numStepsPerEpoch;
    }

    public int getBatchSize(){
        return batchSize;
    }

    public void setBatchSize(int batchSize){
        if(batchSize > 0) this.batchSize = batchSize;
    }

    public int getPatchShape(){
        return patchShape;
    }

    public void setPatchShape(int patchShape) {
        if(patchShape <= 512 && patchShape >= 16 && patchShape % 16 == 0) this.patchShape = patchShape;
    }

    public int getNeighborhoodRadius(){
        return neighborhoodRadius;
    }

    public void setNeighborhoodRadius(int neighborhoodRadius){
        if(neighborhoodRadius > 0) this.neighborhoodRadius = neighborhoodRadius;
    }

    public int getNumberValidation() {
        return numberValidation;
    }

    public void setNumberValidation(int numberValidation) {
        if(numberValidation > 0) this.numberValidation = numberValidation;
    }

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
