/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Authors: William Palmer (William.Palmer@bl.uk)
 *          Alecs Geuder (Alecs.Geuder@bl.uk)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package uk.bl.dpt.qa.flint;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.formats.Format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static uk.bl.dpt.qa.flint.Flint.getAvailableFormats;

/**
 * Command line user interface for flint
 */
public class CoreApp {

    private static Logger LOGGER = LoggerFactory.getLogger(CoreApp.class);

    public static void main(String[] args) {
        LOGGER.info("Java version: {}", getJavaVersion());
        ArgumentParser parser = ArgumentParsers.newArgumentParser("FLint")
                .defaultHelp(true)
                .description("A program to detect potential DRM issues within files.");

        try {
            Map<String, Format> formats = getAvailableFormats();
            parser.addArgument("input")
                .help("Path to file or directory (recursivly searched for files of interest) to be analysed.");
            String outputDefault = ".";
            parser.addArgument("-o", "--output")
                    .help("Where to write the results - in case the specified path is that of a directory," +
                            "a file 'results.xml' will be created within this directory.")
                    .setDefault(outputDefault);
            parser.addArgument("-p", "--policy-properties-dir")
                    .help("Overwrite format-specific policy properties with properties files " +
                            "in the specified directory; the filename has to have the format " +
                            "'<FORMAT_TYPE>-policy.properties', where FORMAT_TYPE can be one of " +
                            formats.keySet());

            File output;
            Namespace ns = parser.parseArgs(args);
            if (ns.getString("output") != null) {
                output = new File(ns.getString("output").trim());
                if (output.isDirectory()) {
                    output = new File(output, "results.xml");
                } else if (!output.exists() && output.getParent() != null) {
                    System.out.println("Output path " + output + " not found, nor the parent directory");
                    System.exit(1);
                }
                LOGGER.info("output: {}", output);
            } else {
                // use current directory
                output = new File(outputDefault);
            }

            try (PrintWriter out = new PrintWriter(new FileWriter(output))) {
                File inputFile = new File(ns.getString("input"));
                if (!inputFile.exists()) {
                    String f =  (inputFile.isDirectory() ? "directory" : "file");
                    System.out.println("Input " + f + " " + inputFile + " not found.");
                    System.exit(1);
                }
                List<List<CheckResult>> resultCollection;
                String ppd = ns.getString("policy_properties_dir");
                if (ppd != null) {
                    resultCollection = Flint.checkMany(inputFile, new File(ppd));
                } else {
                    resultCollection = Flint.checkMany(inputFile, new Flint());
                }
                for (List<CheckResult> results : resultCollection) {
                    Flint.printResults(results, out);
                }
                LOGGER.info("DONE.");
                System.out.println("\ndone. results written to " + output);
            } catch (IOException e) {
                LOGGER.error("can't write results: {}", e);
                System.exit(-1);
            }
        } catch (ArgumentParserException e) {
            if (args.length == 0) {
                parser.printHelp();
            } else {
                parser.handleError(e);
            }
            System.exit(1);

        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * @return the version of the current jvm
     */
    static double getJavaVersion () {
        String version = System.getProperty("java.version");
        return Double.parseDouble(StringUtils.join(ArrayUtils.subarray(version.split("\\."), 0, 2), "."));
    }
}
