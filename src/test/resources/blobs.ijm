
folder = "C:/structure/code/imglib2-labkit/src/test/resources/";

open(folder + "blobs.tif");
run("Classify pixels with Labkit", "dataset=blobs.tif model_file=[" + folder + "blobs.model] use_gpu=true");
