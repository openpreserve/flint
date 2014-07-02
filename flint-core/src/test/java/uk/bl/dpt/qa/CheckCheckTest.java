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
package uk.bl.dpt.qa;

import org.junit.Test;
import uk.bl.dpt.qa.flint.checks.CheckCheck;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by ageuder on 25/06/2014.
 */
public class CheckCheckTest {

    @Test
    public void testResultPassedCountNull() {
        CheckCheck check = new CheckCheck("testCheck", true, null);
        assertThat(check.getName()).isEqualTo("testCheck");
        assertThat(check.isErroneous()).isEqualTo(false);
        assertThat(check.isHappy()).isEqualTo(true);
        assertThat(check.getResult()).isEqualTo("passed");
        assertThat(check.getErrorCount()).isNull();
    }

    @Test
    public void testResultFailedCountOne() {
        CheckCheck check = new CheckCheck("testCheck", false, 1);
        assertThat(check.getName()).isEqualTo("testCheck");
        assertThat(check.isErroneous()).isEqualTo(false);
        assertThat(check.isHappy()).isEqualTo(false);
        assertThat(check.getResult()).isEqualTo("failed");
        assertThat(check.getErrorCount()).isEqualTo(1);
    }

    @Test
    public void testResultErroneousCountZero() {
        CheckCheck check = new CheckCheck("testCheck", null, 0);
        assertThat(check.getName()).isEqualTo("testCheck");
        assertThat(check.isErroneous()).isEqualTo(true);
        assertThat(check.isHappy()).isEqualTo(null);
        assertThat(check.getResult()).isEqualTo("error");
        assertThat(check.getErrorCount()).isEqualTo(0);
    }

}
