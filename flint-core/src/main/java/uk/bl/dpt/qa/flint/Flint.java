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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.dpt.qa.flint.formats.Format;
import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.formats.PolicyAware;
import uk.bl.dpt.qa.flint.wrappers.TikaWrapper;
import static uk.bl.dpt.utils.util.FileUtil.traverse;

/**
 * A program to validate pluggable file formats.
 */
public class Flint {

    private static Logger gLogger = LoggerFactory.getLogger(Flint.class);

    private Collection<Format> formats = new HashSet<Format>();

    /**
     * Create a new FLint object, adding an instance of all formats to the format list
     * for use by check()
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public Flint() throws IllegalAccessException, InstantiationException {
                         formats = getAvailableFormats().values();
                                                                              }

    /**
     * Create a new FLint object, adding an instance of all formats to the format list
     * for use by check()
     *
     * This constructor can overwrite default policy properties if there are property
     * files in the specified `policyDir` that match the formats' file type.
     * @param policyDir directory containing policy files ("formatName-policy.properties")
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws IOException 
     */
    public Flint(File policyDir) throws IllegalAccessException, InstantiationException, IOException {
        formats = getAvailableFormats().values();
        for (Format f : formats) {
            if (f instanceof PolicyAware) {
                final String formatName = f.getFormatName();
                File[] propsFiles = policyDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.equals(formatName + "-policy.properties");
                    }
                });
                if (propsFiles.length == 1) {
                    ((PolicyAware) f).setPatternFilter(propsFiles[0].getAbsolutePath());
                    gLogger.info("found and set a custom policy for {}", f);
                    gLogger.debug("the policy should be: {}", propsFiles[0].getAbsolutePath());
                }
            }
        }
    }

    /**
     * Create a new FLint object, adding an instance of all formats to the format list
     * for use by check()
     *
     * This constructor can overwrite default policy properties for each available format
     * if the supplied map contains a key with its name.
     * @throws InstantiationException 
     * @throws IllegalAccessException 
     */
    public Flint(Map<String, Set<String>> policyMap) throws InstantiationException, IllegalAccessException {
        formats = getAvailableFormats().values();
        for (Format f : formats) {
            if (f instanceof PolicyAware) {
                ((PolicyAware) f).setPatternFilter(policyMap.get(f.getFormatName()));
            }
        }
    }

    /**
     * @return a list of available formats, gathered via *reflection*
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Map<String, Format> getAvailableFormats() throws IllegalAccessException, InstantiationException {
        Map<String, Format> fs = new LinkedHashMap<String, Format>();
        Set<Class<? extends Format>> reflections = new Reflections("uk.bl.dpt.qa.flint.formats").getSubTypesOf(Format.class);
        for (Class<? extends Format> fClass : reflections) {
            Format f = fClass.newInstance();
            gLogger.info("available format {}, as in {}", fClass, fClass);
            fs.put(f.getFormatName(), f);
        }
        return fs;
    }

    /**
     * Check a file using the specific format's check criteria.
     * @param pFile file to check
     * @return a list of {@link uk.bl.dpt.qa.flint.checks.CheckResult}
     */
    public List<CheckResult> check(File pFile) {

        boolean checked = false;

        String mimetype = TikaWrapper.getMimetype(pFile);

        List<CheckResult> results = new ArrayList<CheckResult>();

        gLogger.info("Starting to check file {}..", pFile.getName());
        for(Format format:formats) {
            if(format.canCheck(pFile, mimetype)) {
                gLogger.info("Validating {} with {} checker", pFile.getName(), format.getFormatName());
                CheckResult checkResult = format.validationResult(pFile);
                gLogger.info("check-result: {}", checkResult);
                results.add(checkResult);
                checked = true;
            }
        }

        if(!checked) {
            gLogger.error("Unable to check: {}, mimetype: {}", pFile, mimetype);
        }

        return results;
    }

    public static List<List<CheckResult>> checkMany(File inputFile, Flint flint) throws InstantiationException, IllegalAccessException {
        List<File> files = new LinkedList<File>();
        traverse(inputFile, files);

        List<List<CheckResult>> results = new ArrayList<List<CheckResult>>();

        gLogger.info("Will now search {} files and parse the ones of suitable format.", files.size());
        for(File file:files) {
            gLogger.debug("Checking: {}", file);
            results.add(flint.check(file));
        }
        return results;
    }

    public static List<List<CheckResult>> checkMany(File inputFile, File policyPropertiesDir) throws InstantiationException, IllegalAccessException, IOException {
        Flint flint;
        if (policyPropertiesDir != null) {
            flint = new Flint(policyPropertiesDir);
        } else {
            flint = new Flint();
        }
        return checkMany(inputFile, flint);

    }

    public static List<List<CheckResult>> checkMany(File inputFile, Map<String, Set<String>> policyMap) throws InstantiationException, IllegalAccessException, IOException {
        return checkMany(inputFile, new Flint(policyMap));
    }

    /**
     * Print a set of results as XML to stdout
     * @param pResults results to print
     * @param pOut PrintWriter to send output to
     */
    public static void printResults(List<CheckResult> pResults, PrintWriter pOut) {
        pOut.println("<?xml version='1.0' encoding='utf-8'?>");
        pOut.println("<flint>");
        for(CheckResult result:pResults) {
            result.toXML(pOut, "    ", "    ");
        }
        pOut.println("</flint>");
    }

    public Format getFormat(String format) {
        for (Format f : this.formats) {
            if (f.getFormatName().equals(format)) return f;
        }
        return null;
    }
}
