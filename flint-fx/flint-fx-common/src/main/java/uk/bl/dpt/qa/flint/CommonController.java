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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

import static uk.bl.dpt.qa.flint.wrappers.TikaWrapper.getMimetype;

/**
 * A superclass for the Controllers of flint-fx-direct and flint-fx-websocket providing elements
 * of the common workflow.
 */
public abstract class CommonController implements Initializable {

    protected Logger logger;

    protected File inputFile;
    protected File outputD;
    protected File outFile;

    @FXML
    protected AnchorPane mainStage;
    @FXML
    protected TextField inputPath;
    @FXML
    protected TextField outputPath;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab configTab;
    @FXML
    protected Tab processingTab;
    @FXML
    protected Button goButton;
    @FXML
    protected TextArea logbook;
    @FXML
    protected VBox policyPatterns;

    public CommonController() {
        logger = LoggerFactory.getLogger(getClass());
    }

    protected abstract void askForValidation();

    protected abstract void queryPolicyPatterns();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        inputPath.setOnMouseClicked(event -> {
            FileChooser fChooser = new FileChooser();
            fChooser.setTitle("Select directory to analyse.");
            inputFile = fChooser.showOpenDialog(new Popup());

            if (inputFile != null && inputFile.exists()) {
                // write filename to input textfield
                inputPath.setText(inputFile.getPath());
                // if outputD is also set..
                queryPolicyPatterns();
                if (outputD != null && outputD.exists()) {
                    setReady();
                }
            }
        });

        outputPath.setOnMouseClicked(event -> {
            DirectoryChooser dChooser = new DirectoryChooser();
            dChooser.setTitle("Select direcory to write output in.");
            outputD = dChooser.showDialog(new Popup());

            if (outputD != null && outputD.exists()) {
                // write filename to output textfield
                outputPath.setText(outputD.getPath());
                // if inputFile is also set..
                if (inputFile != null && inputFile.exists()) {
                    setReady();
                }
            }
        });

        goButton.setOnAction(event -> {
            tabPane.getSelectionModel().select(processingTab);
            askForValidation();
        });

    }

    /**
     * Tries to identify the format type of a file.
     * @return the format type in capital letters (e.g. "PDF")
     */
    protected String getFormat() {
        return getMimetype(inputFile).split("/")[1].toUpperCase();
    }

    /**
     * Reset the configuration to the defaults for the detected file format.
     *
     * Creates a new set of checkboxes with default and policy patterns and focuses on the configuration tab.
     */
    public void resetConfiguration(Map<String, Map<String, Set<String>>> polMap) {
        policyPatterns.getChildren().clear();
        Text tickHeadline = new Text("(un)tick policy patterns to validate against.");
        tickHeadline.getStyleClass().add("in-tab-headline");
        policyPatterns.getChildren().add(tickHeadline);
        try {
            String format = getFormat();
            logger.debug("format detected: {}", format);
            logger.debug("polMap: {}", polMap);
            for (Map.Entry<String, Map<String, Set<String>>> pattern : polMap.entrySet()) {
                logger.info("Creating checkbox for policy pattern {}", pattern.getKey());
                CheckBox checkBox = new CheckBox();
                checkBox.setText(pattern.getKey());
                checkBox.setSelected(true);
                policyPatterns.getChildren().add(checkBox);
            }
            tabPane.setVisible(true);
            tabPane.getSelectionModel().select(configTab);
        } catch (Exception e) {
            popupError(e);
        }
    }

    /**
     * make the user controls visible == usable.
     */
    public void setReady() {
        tabPane.setVisible(true);
        tabPane.getSelectionModel().select(configTab);
        goButton.setVisible(true);
    }

    protected void popupError(Exception e) {
        logger.error(e.getMessage());
        Dialogs.create()
                .owner(null)
                .title("Uh, there is an error there!")
                .message(e.getMessage())
                .showException(e);
    }

    /**
     * Parse the checkboxes and add a pattern to the set if the checkbox is ticked.
     * @return a map {formatType: (patternA, patternB, ..)}
     */
    protected Map<String, Set<String>> getCheckedPatterns() {
        Map<String, Set<String>> patterns = new TreeMap<>();
        Set<String> formatPatterns = new TreeSet<>();

        for (Node checkBoxNode : policyPatterns.getChildren()) {
            if (!(checkBoxNode instanceof CheckBox)) continue;
            CheckBox checkBox = (CheckBox) checkBoxNode;
            if (checkBox.isSelected()) {
                formatPatterns.add(checkBox.getText());
            }
        }
        patterns.put(getFormat(), formatPatterns);
        return patterns;
    }

}
