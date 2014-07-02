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

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.utils.PolicyPropertiesCreator;

import javax.websocket.OnMessage;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * This a server-side implementation of a websocket(JSR-356)-based architecture for flint.
 *
 * It answers two questions:
 *  (1) what are the available policy patterns for a specific file-format ({@link #getPolicyPatterns(String)})
 *  (2) what are the validation results for a specific file
 *      ({@link #sendValiationResults(javax.websocket.Session, java.io.InputStream)})
 */
@SuppressWarnings("unused")
@ServerEndpoint("/jobs")
public class WebSocketEndpoint {

    private static Logger LOGGER = LoggerFactory.getLogger(WebSocketEndpoint.class);

    /**
     * @param format expected: the upper-cased end bit of a '/'-split MIME-type string (e.g. 'PDF')
     * @return a json-encoded data-structure (Map<String, Map<String, Set<String>>> ) containing
     *         available policy patterns and sub-hierarchies.
     * @throws IllegalAccessException
     * @throws IOException
     * @throws InstantiationException
     */
    @OnMessage
    public String getPolicyPatterns(String format) throws IllegalAccessException, IOException, InstantiationException {
        LOGGER.info("Received query for policy patterns for format {}", format);
        Map<String, Map<String, Set<String>>> pMap = PolicyPropertiesCreator.getPolicyMap(format);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(pMap);
    }

    /**
     * @param session a running session
     * @param input an input-stream containing binary data of a file to validate
     * @throws IOException
     */
    @OnMessage
    public void sendValiationResults(Session session, InputStream input) throws IOException {
        LOGGER.info("Received : {}", input);
        if (session.isOpen()) {
            session.getAsyncRemote().sendBinary(ByteBuffer.wrap(lint(input).toByteArray()));
        }
    }

    private static ByteArrayOutputStream lint(InputStream input) throws IOException {
        // 1. write the input-stream to a file (nonsense but we do it here as FLint.check
        //    wants a file)
        Path inputF = Files.createTempFile("flint-input", null);
        OutputStream o = new FileOutputStream(inputF.toFile());
        int read;
        byte[] bytes = new byte[1024];
        while((read = input.read(bytes)) != -1) {
            o.write(bytes, 0, read);
        }
        o.close();
        LOGGER.info("starting to lint");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(output);
        try {
            FLint.printResults(new FLint().check(inputF.toFile()), pw);
            LOGGER.info("done with linting");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            output.write(("Caught an error while processing: " + e.getMessage()).getBytes());
        }
        pw.close();
        return output;
    }

    @OnOpen
    public void myOnOpen(Session session) {
        LOGGER.info("WebSocket opened: {}", session.getId());
    }

    @OnClose
    public void myOnClose(CloseReason reason) {
        LOGGER.info("Closing a WebSocket due to {}", reason.getReasonPhrase());
    }
}