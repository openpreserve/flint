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
package uk.bl.dpt.qa.flint.checks;

import java.io.PrintWriter;
import java.util.LinkedHashMap;

import static org.apache.commons.lang3.StringEscapeUtils.ESCAPE_XML10;

/**
 * A set of CheckCategories together forms a
 * {@link uk.bl.dpt.qa.flint.checks.CheckResult}, each category is a container
 * for an arbitrary amount of specific
 * {@link uk.bl.dpt.qa.flint.checks.CheckCheck}.
 *
 * In terms of schematron policy validation a CheckCategory represents a
 * schematron pattern.
 *
 * The category's validation outcome is captured by the
 * method {@link #isHappy()}, which evaluates to true if *none* of the child-tests
 * is unhappy and at least one child-test ran successfully and is happy.
 */
public class CheckCategory {

    private String name;
    private LinkedHashMap<String, CheckCheck> checks;

    /**
     * Create a CheckCategory object, a container for a set of CheckCheck objects
     * @param name the name of this category
     */
    public CheckCategory(String name) {
        this.name = name;
        this.checks = new LinkedHashMap<String, CheckCheck>();
    }

    /**
     * Add a CheckCheck test to this CheckCategory 
     * @param check check to add
     */
    public void add(CheckCheck check) {
        this.checks.put(check.getName(), check);
    }

    /**
     * Test whether or not all the tests in this CheckCatergory were passed 
     * @return  --> true if *none* of the child-tests is unhappy and at least
     * one child-test ran successfully and is happy<br>
     * --> null if there aren't child tests at all or whether they report error(null) regarding their happiness<br>
     * otherwise --> false.
     */
    public Boolean isHappy() {
        if (isErroneous()) return null;
        for (CheckCheck check : this.checks.values()) {
            // if we know of a child-check that ran successfully and FAILED,
            // we are definitely not happy in total.
            if (check != null && !check.isErroneous() && !check.isHappy()) return false;
        }
        return true;
    }

    /**
     * Find out if any CheckCheck in this CheckCategory reports an error
     * @return true if an error is contained in a CheckCheck within this CheckCategory
     */
    public boolean isErroneous() {
        for (CheckCheck check : this.checks.values()) {
            if (!check.isErroneous()) return false;
        }
        return true;
    }

    /**
     * A String representation of the status of this CheckCategory
     * @return "error", "passed" or "failed"
     */
    public String getResult() {
        return this.isErroneous() ? "error" : this.isHappy() ? "passed" : "failed";
    }

    /**
     * Output this CheckCategory as a formatted XML String to a PrintWriter
     * @param pw output
     * @param shift (whitespace) padding output before CheckCategory XML
     * @param indent (whitespace) padding added to "shift" padding for any child CheckCheck output XML
     */
    public void toXML(PrintWriter pw, String shift, String indent) {
        pw.println(String.format("%s<checkCategory name='%s' result='%s'>",
                shift, ESCAPE_XML10.translate(name), getResult()));
        for (CheckCheck check : checks.values()) {
            check.toXML(pw, shift + indent);
        }
        pw.println(String.format("%s</checkCategory>", shift));

    }

    /**
     * Get a CheckCheck object with the associated name
     * @param checkCheckName name of the CheckCheck to retrieve
     * @return the CheckCheck object (or null)
     */
    public CheckCheck get(String checkCheckName) {
        return this.checks.get(checkCheckName);
    }

    /**
     * Get the name of this CheckCategory
     * @return the name of this CheckCategory
     */
    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name + ": " + getResult();
    }
}
