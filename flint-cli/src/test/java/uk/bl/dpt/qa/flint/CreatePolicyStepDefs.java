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

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import junit.framework.TestCase;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class CreatePolicyStepDefs {

    @When("^I call the command line properties file creator with the argument \"([^\"]*)\"$")
    public void I_call_the_command_line_properties_file_creator_with_the_argument(String formatName) throws Throwable {
        PolicyPropertiesCreatorApp.main(new String[] {formatName, "-o " + getClass().getResource("/").getPath()});
    }

    @Then("^a file with the name \"([^\"]*)\" should be created with the following content:$")
    public void a_file_with_the_name_MY_DREAM_policy_properties_should_be_created_with_the_following_content(String fileName, String content) throws Throwable {
        File props = new File(getClass().getResource("/" + fileName).getPath());
        List<String> fileContent = Files.readAllLines(props.toPath(), Charset.defaultCharset());
        String[] contentLines = content.split("\n");
        for (int i=0;i<contentLines.length;i++) {
            TestCase.assertEquals(contentLines[i], fileContent.get(i));
        }
    }
}
