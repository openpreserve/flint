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
package uk.bl.dpt.qa.flint.utils;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.bl.dpt.qa.flint.formats.PolicyAware;
import uk.bl.dpt.qa.flint.formats.Format;

import static uk.bl.dpt.qa.flint.FLint.getAvailableFormats;

/**
 * This class can be used to create a properties file from a schematron policy schema-file.
 * It scans for all asserts and writes them to a {@link java.util.Properties} readable file.
 */
public class PolicyPropertiesCreator {

    private static final Logger logger = LoggerFactory.getLogger(PolicyPropertiesCreator.class);

    /**
     * The namespace for Schematron schemas
     */
    private static final String namespace = "http://purl.oclc.org/dsdl/schematron";


    public static void main(String[] args) {
        try {
            create("src/main/resources/uk.bl.dpt.qa.flint.pdf-policy-validate/policy.properties", null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and returns map from a schematron schema file.
     * <br><br>
     * A schematron file will have a structure like:
     * <[ns:]pattern name="a possibly long patternName">
     *   <[ns:]rule context="ruleContext">
     *     <[ns:]assert test="aTest">text explaining the assert</..>
     *   </..>
     * </..>
     * <br><br>
     * The resulting data-structure will look like: <br/>
     * {"a possibly long patternName":
     *  {"ruleContext":
     *   ("aTest")
     *  }
     * }
     *
     * @param formatName the format's identifier (e.g. "PDF")
     * @return the created file
     * @throws IOException
     */
    public static Map<String, Map<String, Set<String>>> getPolicyMap(final String formatName) throws IOException, InstantiationException, IllegalAccessException {
        final Format format = getAvailableFormats().get(formatName);
        Map<String, Map<String, Set<String>>> policyMap = format.getFixedCategories();
         // where to get the policy hierarchy from
        InputStream policy = ((PolicyAware) format).getPolicy();
        //InputStream stream = PolicyPropertiesCreator.class.getResourceAsStream(pathToPolicyFile);
        //InputStream stream = new FileInputStream(pathToPolicyFile);
        try {
            // how to read the policy file
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true); // need this in order to call doc.getElementsByTagNameNS later
            DocumentBuilder docB = dbf.newDocumentBuilder();
            Document doc = docB.parse(policy);

            // not sure whether the following is needed -- read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            // iterate over the policy patterns
            NodeList patterns = doc.getElementsByTagNameNS(namespace, "pattern");
            for (int p=0;p<patterns.getLength();p++) {
                if (patterns.item(p).getNodeType() != Node.ELEMENT_NODE) continue;
                Element pElem = (Element) patterns.item(p);
                logger.debug("# Pattern: " + pElem.getAttribute("name"));

                // iterate over the rules
                NodeList rules = pElem.getElementsByTagNameNS(namespace, "rule");
                Map<String, Set<String>> ruleMap = new TreeMap<String, Set<String>>();
                for (int r=0;r<rules.getLength();r++) {
                    if (rules.item(r).getNodeType() != Node.ELEMENT_NODE) continue;
                    Element rElem = (Element) rules.item(r);

                    // finally iterate over the asserts
                    NodeList tests = rElem.getElementsByTagNameNS(namespace, "assert");
                    Set<String> testSet = new TreeSet<String>();
                    for (int t=0;t<tests.getLength();t++) {
                        if (tests.item(t).getNodeType() != Node.ELEMENT_NODE) continue;
                        Element tElem = (Element) tests.item(t);
                        // write the assert test as comment..
                        logger.debug("## Assert (test): " + tElem.getAttribute("test"));
                        testSet.add(tElem.getAttribute("test"));
                    }
                    ruleMap.put(rElem.getAttribute("context"), testSet);
                }
                // and write the rule's text as 'id'=true
                logger.debug(pElem.getAttribute("name") + "=true");
                policyMap.put(pElem.getAttribute("name"), ruleMap);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return policyMap;
    }

        /**
     * Creates and returns a properties file from a schematron schema file.
     * <br><br>
     * A schematron file will have a structure like:
     * <[ns:]pattern name="a possibly long patternName">
     *   <[ns:]rule context="ruleContext">
     *     <[ns:]assert test="aTest">text explaining the assert</..>
     *   </..>
     * </..>
     * <br><br>
     * The resulting properties file will look like: <br>
     * # Pattern: a possibly long patternName <br>
     * ## Rule (context): ruleContext <br>
     * ### assert (test): aTest <br>
     * a\ possibly\ long\ patternName=true <br>
     *
     * @param pathToPropertiesFile the properties file path to write to
     * @param formatName the format's identifier (e.g. "PDF")
     * @return the created file
     * @throws IOException
     */
    public static File create(String pathToPropertiesFile, String formatName) throws IOException, IllegalAccessException, InstantiationException {
        // where to write the properties to
        File properties = (pathToPropertiesFile != null) ? new File(pathToPropertiesFile) : File.createTempFile("policies", ".properties");
        logger.info("created properties file at {}", properties.getAbsolutePath());

        // how to write the properties file
        PrintWriter writer= new PrintWriter(properties);
        writer.println("# This properties file is used to filter for specific asserts in the policy validation.");
        writer.println("# All asserts that are set to 'true' will be evaluated.");
        writer.println("");
        logger.info("still alive :-)");

        logger.info("writing policy map with {} patterns", getPolicyMap(formatName).size());
        for (Map.Entry<String, Map<String, Set<String>>> pattern : getPolicyMap(formatName).entrySet()) {
            // write the pattern as comment
            writer.println("# Pattern: " + pattern.getKey());

            for (Map.Entry<String, Set<String>> rule : pattern.getValue().entrySet()) {
                // write the rule as comment
                writer.println("## Rule (context): " + rule.getKey());

                for (String test : rule.getValue()) {
                    // write the assert test as comment..
                    writer.println("## Assert (test): " + test);
                }
            }
            // and write the pattern defaulting to 'true'
            logger.debug("{}=true", pattern.getKey());
            writer.println(pattern.getKey().replaceAll("\\s",  "\\\\ ") + "=true");
            writer.println("");
        }
        writer.close();
        return properties;
    }
}
