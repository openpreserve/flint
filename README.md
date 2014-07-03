
# FLint

[![Build Status](https://travis-ci.org/openplanets/flint.png)](https://travis-ci.org/openplanets/flint)

A modular and extendible file/format validation framework

### What does FLint do?
FLint is a framework to facilitate a configurable file/format validation. It's underlying architecture is based on the idea that file/format validation has nearly always a specific use-case with concrete requirements that may differ from say a validation against the official industry standard of a given format.
The following are the principle ideas we've implemented in order to match such requirements.

#### FLint-the-API
The core module provides an interface for new format-specific implementations, which makes it easy to write a new module. The implementation is provided witha straight-forward core workflow from the input-file to standardised output results. Several optional functionalities (e.g. schematron-based validation, exception and time-out handling of the validation process of corrupt files) help to build a robust validation module.

#### Policy-focused validation
The core module includes (optional) a schematron-based policy-focused validation. 'Policy' in this context means a set of low-level requirements in form of a schematron xml file, that is meant to validate against the xml output of other third-party programs. In this way domain-specific validity requirements can be customised and reduced to the essential.

#### FLint-the-toolbox
Aside internal logic, FLint offers wrappers around a variety of third-party libraries and tools:
* Apache PDFBox
* Apache Tika
* Calibre
* EPUBCheck 
* iText - note that this library is AGPL3 licensed

The wrappers trye to remove the complexity from the specific programs and simplify calls from the core.

All these tools relate more or less to the fields of PDF and EPUB validation, as these are the two existing implementations we're working on at the moment.


#### Format-specific Implementations
* flint-pdf: validation of PDF files using configurable, schematron-based validation of Apache Preflight results and additionally internal logic and all tools in the list above to focus on DRM and Wellformedness
* flint-epub: validation of EPUB files using configurable, schematron-based validation of EPUBCheck results and additionally internal logic and all tools in the list above to focus on DRM and Wellformedness

NOTE: both implementations are work-in-progress and hence far from being satisfactory from a domain-specific point of view, but should be a good guide for how to implement your own format-validation implementation using FLint.


#### The FLint ecosystem
Additionally FLint comes with several 'entry points' that make use of the core functionality
* a Command Line Interface (flint-cli)
* a couple of simple GUIs (flint-fx-direct, flint-fx-websocket)
* a Hadoop MapReduce module (flint-hadoop)


### Who is the intended audience?

FLint is for:

* Content holders
* Preservation experts
* Institutions that need to assess PDF/EPUB files
* Institutions that need to assess files of format X and would like to write their own implementation

## How to install and use

### Requirements

To install you need:

* Git client
* Java Developers Kit
  JDK6+ -- NOTE that flint-hadoop and dependend modules are restricted to 6, whereas flint-cli and flint-serve
  need Java7, and flint-fx* requires java8 -- we're planning to not support java6 anymore in the near future.
* Maven
* Currently you will need a copy of (https://github.com/bl-dpt/dptutils) installed in your local maven repo

### Build

To download and compile execute the commands:

```bash
$ git clone https://github.com/bl-dpt/flint.git
$ cd flint
$ mvn clean install
```

After successful compile the binaries will be available in the `target/` directory of each submodule and in your local maven repository.
From here you could get familiar with the command-line interface typing

```bash
$ java -jar flint-cli/target/flint-cli-<version>-jar-with-dependencies.jar
```

This will give you an overview over the options you have to play around with.

## More information

### Licence

FLint is released under [Apache version 2.0 license](LICENSE.txt).  *Note that dependencies have different licenses*

### Acknowledgements

Part of this work was supported by the European Union in the 7th Framework Program, IST, through the SCAPE project, Contract 270137.


### Contribute

1. [Fork the GitHub project](https://help.github.com/articles/fork-a-repo)
2. Change the code and push into the forked project
3. [Submit a pull request](https://help.github.com/articles/using-pull-requests)

### Misc notes

Jhove1:
Install using Maven using repo: https://github.com/openplanets/jhove
