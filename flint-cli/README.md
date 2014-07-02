FLintCli - Command line interface for FLint
===========================================

Overview
--------
The mvn install command creates two jars with dependencies, one representing
the core application, and one to generate a properties file, that can be
modified prior to executing the core app.

Installation
------------
$ mvn clean install

The core app command line interface
-----------------------------------
To get an overview over usage and accepted parameters, go to flint-cli's
`target` directory and type:
$ java -jar flint-cli-<version>-jar-with-dependencies.jar -h

Create a policy properties file
-------------------------------
FLint ships with a default policy definition for each of the supported file
formats. In order to reduce the tested patterns to a subset of patterns of
interest, one can create a properties file that collects all available patterns
from the default policy.
To get an overview over usage and accepted parameters, from flint-cli's target
directory type:
$ java -jar create-policy-properties-file.jar -h

