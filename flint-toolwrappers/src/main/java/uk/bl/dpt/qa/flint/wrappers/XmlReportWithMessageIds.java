/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Author: William Palmer (William.Palmer@bl.uk)
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
package uk.bl.dpt.qa.flint.wrappers;

import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.MessageLocation;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.XmlReportImpl;

import java.io.File;

/**
 * The single purpose of this class is to report messages using the messageIds
 * (based on Enums) as message content.
 */
public class XmlReportWithMessageIds extends XmlReportImpl {

    public XmlReportWithMessageIds(File out, String ePubName, String versionEpubCheck) {
        super(out, ePubName, versionEpubCheck);
    }

    @Override
    public void message(Message message, MessageLocation location, Object... arg) {

    	class MyMessage extends Message {
            public MyMessage(MessageId messageId, Severity severity, String message, String suggestion) {
                super(messageId, severity, messageId.name(), suggestion);
            }
        }
        
        Message myMessage = new MyMessage(message.getID(), message.getSeverity(), message.getMessage(), message.getSuggestion());
        super.message(myMessage, location, arg);
    }

}
