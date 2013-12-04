
# DRMLint

[![Build Status](https://travis-ci.org/bl-dpt/drmlint.png)](https://travis-ci.org/bl-dpt/drmlint)

Check for format validity and detect DRM in PDF and EPUB files

### What does X do?

DRMLint uses a variety of libraries and tools, as well as internal logic, to assess PDF/EPUB files:
* Apache PDFBox
* Apache Tika
* Calibre
* EPUBCheck 
* (Optionally) iText - note that this library is AGPL3 licensed

### Who is intended audience?

DRMLint is for:

* Content holders
* Preservation experts
* Institutions that need to assess PDF/EPUB files

## Features and roadmap

### Version 0.1.0

* More robuest checking of PDF and EPUB files

### Roadmap

* Feature ?

## How to install and use

### Requirements

To install you need:

* JDK6+
* Maven

## More information

### Licence

DRMLint is released under [Apache version 2.0 license](LICENSE.txt).  *Note that dependencies have different licenses*

### Acknowledgements

Part of this work was supported by the European Union in the 7th Framework Program, IST, through the SCAPE project, Contract 270137.

### Requirements

To build you require:

* Git client
* Apache Maven
* Java Developers Kit (e.g. OpenJDK 6)

### Build

To download and compile execute the commands:

```bash
$ git clone https://github.com/bl-dpt/drmlint.git
$ cd drmlint
$ mvn clean install package
```

After successful compile the binary will be available in the `target/` directory.

### Contribute

1. [Fork the GitHub project](https://help.github.com/articles/fork-a-repo)
2. Change the code and push into the forked project
3. [Submit a pull request](https://help.github.com/articles/using-pull-requests)

### Misc notes

To execute with Jhove2:
java -cp .;config/;config/droid/;target\drmlint-0.0.1-SNAPSHOT-jar-with-dependencies.jar uk.bl.dpt.qa.drmlint.DRMLint

Jhove1:
Install using Maven using repo: https://github.com/openplanets/jhove
 
TODO: Add an enum for return codes after checking - true/false/unknown for better reporting
