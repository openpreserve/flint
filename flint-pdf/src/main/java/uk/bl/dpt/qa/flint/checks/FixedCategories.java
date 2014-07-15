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

/**
 * There are two types of Check Categories:
 * (1) dynamically defined ones from within a (possibly changing) schematron
 *     policy (aka schematron patterns)
 * (2) additional static ones that require specific check methods and are added
 *     'manually' to the dynamic list; these ones are the FixedCategories.
 */
public enum FixedCategories {
	
	/**
	 * Category for testing well-formedness
	 */
	WELL_FORMED("well-formed"),
	/**
	 * Category for checking for DRM
	 */
    NO_DRM("specific-drm-checks"),
    /**
     * Category for validating against a policy
     */
    POLICY_VALIDATION("policy-validation");

    private final String cat;

    FixedCategories(String cat) {
        this.cat = cat;
    }

    public String toString() {
        return cat;
    }
    
}
