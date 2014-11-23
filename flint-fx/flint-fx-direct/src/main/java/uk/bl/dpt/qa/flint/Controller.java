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

import javafx.application.Platform;
import javafx.concurrent.Task;
import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.utils.PolicyPropertiesCreator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.bl.dpt.qa.flint.wrappers.TikaWrapper.getMimetype;

/**
 * A controller for the flint-fx GUI that directly runs the flint classes.
 */
public class Controller extends CommonController {

    // content of the logbook, stored and modified in a very odd way
    String logBookContent = "";

    public void askForValidation() {
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() {
                PrintWriter out;
                try {
                    logBookContent += "\n--> processing file " + inputFile.getName();
                    updateMessage(logBookContent);
                    List<CheckResult> results = new Flint(getCheckedCategories()).check(inputFile);
                    outFile = new File(outputD, "flint_results_" + inputFile.getName() + ".xml");
                    out = new PrintWriter(new FileWriter(outFile));
                    logger.info("Analysis done, results: {}", results);
                    CheckResult res = results.get(0);
                    Boolean happy = Boolean.FALSE;
                    if(res!=null){
                    	happy = res.isHappy(); //results.get(0).isHappy();
                    }
                    String passed = ((happy!=null)&&happy ? "*passed*." : "*failed*.");
                    logBookContent += "\n    Analysis done, overall result: " + passed;
                    updateMessage(logBookContent);
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                        	setResults(results);
                        }
                    });
                    Flint.printResults(results, out);
                    out.close();
                    logger.info("results written to {}", outFile.getPath());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    updateMessage("an exception ocurred: " + e.getMessage());
                }
                return null;
            }
            @Override protected void succeeded() {
                logBookContent += "\n    results written to " + outFile.getName();
                updateMessage(logBookContent);
            }
        };
        logbook.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }

    @Override
    protected Collection<String> getAvailableFormats() {
        Collection<String> formats = null;
        try {
             formats = Flint.getAvailableFormats(getMimetype(inputFile)).keySet();
        } catch (IllegalAccessException | InstantiationException e) {
            logger.error(e.getMessage());
            popupError(e);
        }
        return formats;
    }

    @Override
    public void queryPolicyCategories(String format) {
        Task<Map<String, Map<String, Set<String>>>> task = new Task<Map<String, Map<String, Set<String>>>>() {
            @Override protected Map<String, Map<String, Set<String>>> call() {
                try {
                    return PolicyPropertiesCreator.getPolicyMap(format);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                return null;
            }
            @Override protected void succeeded() {
                resetConfiguration(this.getValue());
            }
        };
        new Thread(task).start();
    }

}
