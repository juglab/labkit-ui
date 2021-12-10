[![](https://github.com/juglab/labkit-ui/actions/workflows/build-main.yml/badge.svg)](https://github.com/juglab/labkit-ui/actions/workflows/build-main.yml)

# Labkit (UI)

An advanced Fiji plugin for image segmentation. 

**Tutorials, installation instructions and documentation can be found on https://imagej.net/plugins/labkit.**

## Project Structure

This github repository contains the Labkit user interface / Fiji plugin. There are two other repositories that are related to Labkit:

* https://github.com/juglab/labkit-pixel-classification contains the implementation of the pixel classification algorithm that Labkit uses for automatic segmentation.
* https://github.com/juglab/labkit-command-line contains a command line interface, that can be used to execute the Labkit pixel classification algorithm on the command line.

## Software Developement

If you developed a segmentation algorithm, but you don't want to develope your own UI. You might just use the Labkit UI, see this example:

[CustomSegmenterDemo.java](https://github.com/juglab/labkit-ui/blob/master/src/test/java/demo/custom_segmenter/CustomSegmenterDemo.java)

(and don't hesitate to contact us ;)
