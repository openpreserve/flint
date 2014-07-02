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


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A CheckResult summarises the outcome of a range of checks on a particular file.
 *
 * It contains a set of checks ({@link CheckCheck}), categorised
 * in categories ({@link CheckCategory}).
 *
 * The outcome of the CheckResult is expressed in the method {@link CheckResult#isHappy()},
 * which evaluates to true if the isHappy methods of all checks in the assigned categories
 * evaluate to true.
 *
 * A CheckResult knows how to be written as an xml string ({@link CheckResult#toXML(java.io.PrintWriter, String, String)}).
 */
public class CheckResult {

    private String filename;
    private String format;
    private String version;
    private Long time; // can be null

    // general information in a checkresult(.toMap), other then categories,
    // always available
    public static final String[] fixedResultBits = new String[]{
        "filename", "format", "version", "result", "timeTaken"
    };

    LinkedHashMap<String, CheckCategory> categories;

    /**
     * inits a check-result
     *
     * @param filename name of the input file
     * @param format string representation of the format (e.g. "PDF", "EPUB", ..)
     * @param version version of the toolkit producing the check-result
     */
    public CheckResult(String filename, String format, String version) {
        this.filename = filename;
        this.format = format;
        this.version = version;
        this.categories = new LinkedHashMap<String, CheckCategory>();
    }

    /**
     * inits a check-result with expected categories, which fills the keys of the categories map
     * with their names.
     *
     * @param filename name of the input file
     * @param format string representation of the format (e.g. "PDF", "EPUB", ..)
     * @param version version of the toolkit producing the check-result
     * @param expectedCategories list of strings representing expected category names
     */
    public CheckResult(String filename, String format, String version, Collection<String> expectedCategories) {
        this(filename, format, version);
        for (String cat : expectedCategories) {
            this.categories.put(cat, null);
        }
    }

    public void add(CheckCategory cc) {
        this.categories.put(cc.getName(), cc);
    }
    public void addAll(Map<String, CheckCategory> ccMap) {
        this.categories.putAll(ccMap);
    }

    /**
     * @return  --> true if *none* of the child-categories is unhappy and at least
     * one child-category ran successfully and is happy, --> null if there aren't child
     * categories at all or whether they report error(null) regarding their happiness,
     * otherwise --> false.
     */
    public Boolean isHappy() {
        if (isErroneous()) return null;
        for (CheckCategory cc : this.categories.values()) {
            // if we know of a child-check that ran successfully and FAILED,
            // we are definitely not happy in total.
            if (cc != null && !cc.isErroneous() && !cc.isHappy()) return false;
        }
        return true;
    }

    public boolean isErroneous() {
        for (CheckCategory cc : this.categories.values()) {
            if (!cc.isErroneous()) return false;
        }
        return true;
    }

    public String getResult() {
        return this.isErroneous() ? "erroneous" : this.isHappy() ? "passed" : "failed";
    }

    public void toXML(PrintWriter pw, String shift, String indent) {
        pw.println(String.format("%s<checkedFile name='%s' result='%s' format='%s' version='%s' totalCheckTime='%s'>",
                shift, getFilename(), getResult(), getFormat(), getVersion(), getTimeTaken()));
        for (CheckCategory cc : this.categories.values()) {
            if (cc != null) cc.toXML(pw, shift + indent, indent);
        }
        pw.println(String.format("%s</checkedFile>", shift));
    }

    public CheckCategory get(String catName) {
        return categories.get(catName);
    }

	public String toString() {
        List<String> cats = new ArrayList<String>();
        for (CheckCategory cc : this.categories.values()) {
            if (cc != null) {
                cats.add(cc.toString());
            }
        }
        return String.format("%s: v%s, %s, %s, time: %s ms",
                getFormat(), getVersion(), getFilename(), StringUtils.join(cats, ", "), getTimeTaken());
	}

    public LinkedHashMap<String, String> toMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        for (String s : fixedResultBits) {
            try {
                Method func = this.getClass().getMethod("get" + WordUtils.capitalize(s));
                map.put(s, (String) func.invoke(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, CheckCategory> entry: this.categories.entrySet()) {
            CheckCategory cc = entry.getValue();
            map.put(entry.getKey(), (cc == null ? "" : cc.getResult()));
        }
        return map;
    }

    /**
	 * Get filename
	 * @return filename
	 */
	public String getFilename() {
		return this.filename;
	}
	/**
	 * Get format object used for the checks
	 * @return name of format object
	 */
	public String getFormat() {
		return this.format;
	}
	/**
	 * Version of format object used for the checks
	 * @return Version of format object used for the checks
	 */
	public String getVersion() {
		return this.version;
	}
	/**
	 * Get the time taken to execute tests (in ms)
	 * @return time taken to execute tests (in ms)
	 */
	public String getTimeTaken() {
		return Long.toString(this.time);
	}
    public void setTime(Long time) {
        this.time = time;
    }
}
