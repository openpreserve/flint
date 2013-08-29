
To execute with Jhove2:
java -cp .;config/;config/droid/;target\drmlint-0.0.1-SNAPSHOT-jar-with-dependencies.jar uk.bl.dpt.qa.drmlint.DRMLint

Jhove1:
Install using Maven using repo: https://github.com/openplanets/jhove

Jhove2:
Install using Maven
Resolution for single-jar files is in maven file (shaded jar; issues with spring schemas etc)
Jhove 2 doesn't work with PDFs - but wrapper code done

Create config jars (this doesn't work yet):
(putting these in the classpath instead of the directories doesn't seem to work)
jar -cf jhove2-config.jar -C config .
jar -cf jhove2-droid-config.jar -C config/droid .
 
Epubcheck:
Needs a fix to 3.0.1 otherwise it will crash.  See http://code.google.com/p/epubcheck/issues/detail?id=295