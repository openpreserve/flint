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
package uk.bl.dpt.qa.flint.epub.checks;

/**
 * There are two types of Check Categories:
 * (1) dynamically defined ones from within a (possibly changing) schematron
 *     policy (aka schematron patterns)
 * (2) additional static ones that require specific check methods and are added
 *     'manually' to the dynamic list; these ones are the FixedCategories.
 */
public enum FixedCategories {
    WELL_FORMED_CALIBRE("Well formed according to Calibre"),
    NO_DRM_RIGHTS_FILE("DRM check looking for rights file"),
    POLICY_VALIDATION("Overall error indicator for policy validation");

    private final String cat;

    FixedCategories(String cat) {
        this.cat = cat;
    }

    public String toString() {
        return cat;
    }
}
