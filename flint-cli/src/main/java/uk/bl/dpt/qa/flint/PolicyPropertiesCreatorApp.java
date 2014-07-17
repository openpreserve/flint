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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.formats.Format;
import uk.bl.dpt.qa.flint.utils.PolicyPropertiesCreator;

import java.io.File;
import java.util.Map;

import static uk.bl.dpt.qa.flint.Flint.getAvailableFormats;

/**
 * Generates a properties file from a schematron policy of a given format in the directory
 * from which the command is executed.
 */
public class PolicyPropertiesCreatorApp {
    private static Logger LOGGER = LoggerFactory.getLogger(PolicyPropertiesCreatorApp.class);

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("PolicyPropertiesCreatorApp")
                .defaultHelp(true)
                .description("Creates a modifiable properties file from the default" +
                        " schematron policy.");
        try {
            Map<String, Format> formats = getAvailableFormats();
            parser.addArgument("format")
                    .choices(formats.keySet())
                    .help("Specify format to generate policy properties for.");
            String outputDefault = ".";
            parser.addArgument("-o", "--output")
                    .help("Where to write the properties file - in case the specified path is that of a directory," +
                            "a file 'results.xml' will be created within this directory.")
                    .setDefault(outputDefault);
            File output;
            Namespace ns = parser.parseArgs(args);
            String format = ns.getString("format");
            if (ns.getString("output") != null) {
                output = new File(ns.getString("output").trim());
                if (output.isDirectory()) {
                    output = new File(output, format + "-policy.properties");
                } else if (!output.exists() && output.getParent() != null) {
                    System.out.println("Output path " + output + " not found, nor the parent directory");
                    System.exit(1);
                }
                LOGGER.info("output: {}", output);
            } else {
                // use current directory
                output = new File(outputDefault);
            }

            PolicyPropertiesCreator.create(output.getAbsolutePath(), format);

            System.out.println("\nCreated file " + format + "-policy.properties");
        } catch (ArgumentParserException e) {
            if (args.length == 0) {
                parser.printHelp();
            } else {
                parser.handleError(e);
            }
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }

    }
}
