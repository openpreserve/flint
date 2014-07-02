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
package uk.bl.dpt.qa.flint.hadoop;

/**
 * This is for version 2-0-0.cdh4-2-0
 */
public class Hadoop2_0_0_CDH4_2_0 implements HadoopVersion {

    protected static final String LINES_PER_MAP = "mapreduce.input.lineinputformat.linespermap";

    // NOTE that "mapreduce.task.timeout" does not work; why?? is it that the server is set up in
    // a specific way?
    protected static final String TASK_TIMEOUT = "mapred.task.timeout";

    @Override
    public String linesPerMap() {
        return LINES_PER_MAP;
    }

    @Override
    public String taskTimeout() {
        return TASK_TIMEOUT;
    }
}
