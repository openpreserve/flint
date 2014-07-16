
# Flint

[![Build Status](https://travis-ci.org/openplanets/Flint.png)](https://travis-ci.org/openplanets/Flint)

A modular and extendible file/format validation framework

## What does Flint do?
Flint is a framework to facilitate a configurable file/format validation. Its underlying architecture is based on the idea that file/format validation almost always has a specific use-case with concrete requirements that may differ from a validation against the official industry standard of a given format. The following are the principle ideas we've implemented in order to match such requirements.

### Who is the intended audience?

Flint is for:

* Content holders
* Preservation experts
* Institutions that need to assess PDF/EPUB files
* Institutions that need to assess files of format X and would like to write their own implementation

### Adding file formats to Flint
The core module provides an interface for new format-specific implementations, which makes it easy to write a new module. The implementation is provided with a straight-forward core workflow from the input-file to standardised output results. Several optional functionalities (e.g. Schematron-based validation, exception and time-out handling of the validation process of corrupt files) help to build a robust validation module.

### Policy-focused validation
The core module includes (optional) a Schematron-based policy-focused validation. 'Policy' in this context means a set of low-level requirements in form of a Schematron XML file, that is meant to validate against the XML output of other third-party programs. In this way domain-specific validity requirements can be customised and reduced to the essential.

### Reusing external libraries
In addition to Flint's internal logic, Flint offers wrappers around a variety of third-party libraries and tools, including:
* Apache PDFBox
* Apache Tika
* Calibre
* EPUBCheck 
* iText - note that this library is AGPL3 licensed

Each wrapper neatly encapsulates the code required to use each dependency, removing complexity and simplifying calls from the core.

All these tools relate more or less to the fields of PDF and EPUB validation, as these are the two existing implementations we're working on at the moment. Reusing several different sources of information in this way creates a more robust result than relying on any single source.

### Currently Supported file formats
* Flint-pdf: validation of PDF files using configurable, Schematron-based validation of Apache Preflight results and additionally internal logic and all tools in the list above to focus on DRM and Wellformedness
* Flint-epub: validation of EPUB files using configurable, Schematron-based validation of EPUBCheck results and additionally internal logic and all tools in the list above to focus on DRM and Wellformedness

NOTE: both implementations are work-in-progress and hence far from being satisfactory from a domain-specific point of view, but should be a good guide for how to implement your own format-validation implementation using Flint.

## Ways you can use Flint
Flint comes with several 'entry points' that make use of the core functionality
* a Command Line Interface (flint-cli)
* a simple GUI, using JavaFX8 (flint-fx-direct, flint-fx-websocket)
* a Hadoop MapReduce module (flint-hadoop)

## How to install and use

### Requirements

To install you need:

* Git client
* Java Developers Kit
  JDK6+ -- NOTE flint-hadoop and its dependencies are restricted to Java 6, whereas flint-cli and flint-serve
  need Java 7, and Flint-fx* requires Java 8.  We are planning to drop Java 6 support soon.
* Maven
* Currently you will need a copy of (https://github.com/bl-dpt/dptutils) installed in your local maven repo

### Build

To download and compile execute the commands:

```bash
$ git clone https://github.com/openplanets/flint.git
$ cd flint
$ mvn clean install
```

After successful compile the binaries will be available in the `target/` directory of each submodule and in your local maven repository.
From here you could get familiar with the command-line interface typing

```bash
$ java -jar flint-cli/target/flint-cli-<version>-jar-with-dependencies.jar
```

This will give you an overview over the options you have to play around with.

### Misc build notes

Jhove1:
Install using Maven using repo: https://github.com/openplanets/jhove-old

## More information

### Licence

Flint is released under [Apache version 2.0 license](LICENSE.txt).  *Note that dependencies have different licenses*

### Acknowledgements

Part of this work was supported by the European Union in the 7th Framework Program, IST, through the SCAPE project, Contract 270137.

### Contribute

1. [Fork the GitHub project](https://help.github.com/articles/fork-a-repo)
2. Change the code and push into the forked project
3. [Submit a pull request](https://help.github.com/articles/using-pull-requests)
