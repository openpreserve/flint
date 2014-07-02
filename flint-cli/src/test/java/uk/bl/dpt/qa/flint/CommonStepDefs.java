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

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import uk.bl.dpt.qa.flint.formats.MyDreamFormat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static junit.framework.TestCase.assertTrue;
import static uk.bl.dpt.qa.flint.FLint.getAvailableFormats;

/**
  * Common step defs for testing flint-cli
  */
 public class CommonStepDefs {

    @Given("^I have a class that implements Format and knows about the \"([^\"]*)\" format$")
    public void I_have_a_class_that_implements_Format_and_knows_about_the_format(String formatName) throws Throwable {
        assertTrue(getAvailableFormats().containsKey(formatName));
    }

    @And("^the related schematron policy file is:$")
    public void the_related_schematron_policy_file_is(String policy) throws Throwable {
        File policyFile = new File(MyDreamFormat.class.getResource("/").getPath() + "/test-policy-file");
        Files.write(policyFile.toPath(), policy.getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

 }
