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

import java.io.File;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;

/**
 * Helper class to perform timed validations that can be killed after a certain
 * timeout[seconds] has been reached. To be subclassed and used together with
 * {@link uk.bl.dpt.qa.flint.checks.TimedValidation}.
 */
public abstract class TimedTask implements Callable<LinkedHashMap<String, CheckCategory>> {

    protected String name;
    protected File contentFile;
    protected long timeout;

    /**
     * Create a new TimedTask object
     * @param name name of the task
     * @param timeout timeout before task is terminated
     */
    public TimedTask(String name, long timeout) {
        super();
        this.name = name;
        this.timeout = timeout;
    }

    /**
     * Set the file that will be used by the TimedTask
     * @param contentFile file to be used by the TimedTask
     */
    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
    }
    
}