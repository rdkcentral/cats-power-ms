package com.cats.power.utils;

/*
 * Copyright 2021 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Websocket client to connect to the Rack Manager.
 */
public class WebsocketClient {
    private final String RACK_MANAGER_HOST              = "ws://172.17.0.1:8088";
    
    private WebsocketClientEndpoint clientEndPoint = null;

    /**
     * Constructor.
     * @param relUri The relative URI to connect to.
     */
    public WebsocketClient(String relUri){
        try {
            System.out.println("Connecting to : " + new URI(RACK_MANAGER_HOST + relUri).toString());
            clientEndPoint = new WebsocketClientEndpoint(new URI(RACK_MANAGER_HOST + relUri));
            // add listener
            clientEndPoint.addMessageHandler(System.out::println);
        } catch (URISyntaxException ex) {
            Logger.getLogger(WebsocketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send a message to the Rack Manager.
     * @param message The message to send.
     */
   public void sendMessage(String message){
       clientEndPoint.sendMessage(message);
   }
}
