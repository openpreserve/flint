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

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import javafx.application.Platform;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.websocket.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
 * This is a websocket specific controller for flint-fx. It builds on top of the
 * JSR-356 specifications.
 *
 * There are two communication 'channels':
 *  --> a query for policy patterns specific to a file format ({@link #queryPolicyPatterns()})
 *      the response is dealt with asynchronously in {@link #setPolicyPatterns(String)}
 *  --> the validation query for a specific file ({@link #askForValidation()})
 *      the response is dealt with asynchonously in {@link #printValidationResults(java.io.InputStream)}
 *
 * NOTE: In order to run successfully it obviously needs a running server-side implementation,
 * as provided by flint-serve.
 */
@ClientEndpoint
public class Controller extends CommonController {

    final static String serverUri = "ws://localhost:9000/jobs";

    private javax.websocket.Session session;

    public void askForValidation() {
        connectToWebSocket();
        try (InputStream input = new FileInputStream(inputFile);
             OutputStream output = session.getBasicRemote().getSendStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
            logger.info("Sent file {} to server", inputFile);
            Platform.runLater(() -> logbook.setText(logbook.getText() +
                    ("\n--> Sent file " + inputFile + " to server")));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void queryPolicyPatterns() {
        connectToWebSocket();
        try {
            session.getBasicRemote().sendText(getFormat());
        } catch (Exception e) {
            logger.error(e.getMessage());
            popupError(e);
        }
    }

    @OnMessage
    public void setPolicyPatterns(String jsonObject) {
        ObjectMapper mapper = new ObjectMapper();
        logger.info("got policy patterns as jsonObject: {}", jsonObject);
        Platform.runLater(() -> {
            try {
                resetConfiguration(mapper.readValue(jsonObject, new TypeReference<Map<String, Map<String, Set<String>>>>() {
                }));

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @OnOpen
    @SuppressWarnings("unused")
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    @SuppressWarnings("unused")
    public void printValidationResults(InputStream input) throws IOException {
        logger.info("WebSocket message received - processing");
        Platform.runLater(() -> logbook.setText(logbook.getText() +
                "\n    received results - processing"));
        File outputFile = new File(outputD, "results-" + inputFile.getName() + ".xml");
        Files.write(ByteStreams.toByteArray(input), outputFile);

        logger.info("results written to: ", outputFile);
        Platform.runLater(() -> logbook.setText(logbook.getText() +
                ("\n    results written to: " + outputFile)));
        for (String line : Files.readLines(outputFile, Charset.defaultCharset())) {
            logger.info(line);
        }
    }

    private void connectToWebSocket() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = URI.create(serverUri);
        try {
            container.connectToServer(this, uri);
        } catch (DeploymentException | IOException | NullPointerException e) {
            logger.error(e.getMessage());
            popupError(new Exception("ERROR. server not running?"));
        }
    }

}
