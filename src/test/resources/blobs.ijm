
folder = "C:/structure/code/imglib2-labkit/src/test/resources/";

open(folder + "blobs.tif");
run("Classify pixels with Labkit", "dataset=blobs.tif modelfile=[" + folder + "blobs.model]");
