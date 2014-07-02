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

import com.google.common.io.Files;
import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import uk.bl.dpt.qa.flint.formats.Format;
import uk.bl.dpt.qa.flint.formats.SimpleFormatNoPolicy;
import uk.bl.dpt.qa.flint.formats.SimpleFormatWithPolicy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;

import static org.fest.assertions.Assertions.assertThat;

/**
  * Created by ageuder on 23/06/2014.
  */
public class CoreAppStepDefs {

    File tempDir;
    SimpleFormatWithPolicy formatWithPolicy;

    @Before
    public void setUp() {
        tempDir = Files.createTempDir();
        formatWithPolicy = new SimpleFormatWithPolicy();
    }

    @Given("^which has only one check category \"(.*?)\" that is always happy, if not supplied with a schematron policy$")
        public void which_has_only_one_check_category_that_is_always_happy_if_not_supplied_with_a_schematron_policy(String catName) throws Throwable {
            assertThat(new SimpleFormatNoPolicy().getAllCategoryNames())
                    .hasSize(1)
                    .contains(catName);
        }

    @Given("^which exclusively does policy-based validation and uses the following schema:$")
    public void which_exclusively_does_policy_based_validation_and_uses_the_following_schema(String schema) throws Throwable {
        assertThat(formatWithPolicy.getPolicyAsString().replaceAll("\r\n|\n|\\s+", "")).isEqualTo(schema.replaceAll("\r\n|\n|\\s+", ""));
    }

    @Given("^I have a file \"(.*?)\" with the following content:$")
        public void i_have_a_file_with_the_following_content(String fileName, String content) throws Throwable {
            FileUtils.write(new File(tempDir, fileName), content);
        }

    @When("^I call the command line CoreApp specifying the file's location \"(.*?)\"$")
        public void i_call_the_command_line_CoreApp_specifying_the_file_s_location(String filePath) throws Throwable {
            CoreApp.main(new String[]{new File(tempDir, filePath).getAbsolutePath(), "-o " + tempDir.getAbsolutePath()});
        }

    @Then("^a results file \"(.*?)\" should be produced with the following content:$")
        public void a_results_file_should_be_produced_with_the_following_content(String fileName, String expectedContent) throws Throwable {
            File resultsFile = new File(tempDir, fileName);
            String results = FileUtils.readFileToString(resultsFile, Charset.defaultCharset());
            assertThat(results.replaceAll("\r\n|\n|\\s+", "")).isEqualTo(expectedContent.replaceAll("\r\n|\n|\\s+", ""));
        }

}
