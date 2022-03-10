[![](https://github.com/juglab/labkit-ui/actions/workflows/build-main.yml/badge.svg)](https://github.com/juglab/labkit-ui/actions/workflows/build-main.yml)

# Labkit (UI)

An advanced Fiji plugin for image segmentation.

![](https://user-images.githubusercontent.com/24407711/133519201-67d6e29f-f024-4803-8eee-75831a996952.gif)

## Tutorials, installation instructions and documentation

**>> Can be found on: https://imagej.net/plugins/labkit.**


## How to cite

Please cite our paper: [LABKIT: Labeling and Segmentation Toolkit for Big Image Data](https://www.frontiersin.org/article/10.3389/fcomp.2022.777728)

```
@article{arzt2022labkit
  author={Arzt, Matthias and Deschamps, Joran and Schmied, Christopher and Pietzsch, Tobias and Schmidt, Deborah and Tomancak, Pavel and Haase, Robert and Jug, Florian},   
  title={LABKIT: Labeling and Segmentation Toolkit for Big Image Data},      
  journal={Frontiers in Computer Science},      
  volume={4},      
  year={2022},      
  url={https://www.frontiersin.org/article/10.3389/fcomp.2022.777728},       
  doi={10.3389/fcomp.2022.777728},      
  issn={2624-9898},   
}
```

## Project Structure

* This github repository contains the Labkit user interface / Fiji plugin.
* https://github.com/juglab/labkit-pixel-classification contains the implementation of the pixel classification algorithm.
* https://github.com/juglab/labkit-command-line contains a command line interface, that can be used to execute the Labkit pixel classification algorithm on the command line and HPC clusters.

## Software Developement

If you developed a segmentation algorithm, but you don't want to develope your own UI. You might just use the Labkit UI, see this example:

[CustomSegmenterDemo.java](https://github.com/juglab/labkit-ui/blob/master/src/test/java/demo/custom_segmenter/CustomSegmenterDemo.java)

(and don't hesitate to contact us ;)
