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

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.slf4j.LoggerFactory;

/**
 * WebsocketClientEndpoint provides a simple websocket client
 * to connect to a device over Websocket.
 * */
@ClientEndpoint
public class WebsocketClientEndpoint {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger( WebsocketClientEndpoint.class );
    Session userSession = null;
    private MessageHandler messageHandler;
    private URI endpointURI;
    private WebSocketContainer container;
    
    private int connectRetry = 1;
    private int retryDelay = 100;

    /**
     * Constructor.
     * @param endpointURI The URI to connect to.
     */
    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            this.endpointURI = endpointURI;
            this.container = ContainerProvider.getWebSocketContainer();
            connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Connect to the websocket.
     */
    public void connect(){
        try {
            /**
             * Implement exponential backoff to prevent server flooding due to 
             * rack-recon node going offline. Random variable introduced to prevent
             * all clients from simultaneously attempting to reconnect.
             **/
            container.connectToServer(this, endpointURI);
            log.info("Websocket connection established");
            connectRetry = 1;
        } catch (DeploymentException | IOException ex) {
            try {
                long sleepTime = (long) Math.floor(retryDelay*connectRetry*connectRetry*Math.random());
                Thread.sleep(sleepTime);
                log.trace("Websocket connection failed to establish, retrying in: " + sleepTime + "ms");
                connectRetry++;
                connect();
            } catch (InterruptedException ex1) {
                Logger.getLogger(WebsocketClientEndpoint.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
    
    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        try {
            log.error("Websocket closed due to: " + reason.getReasonPhrase() + ", reconnecting");
            this.userSession = null;
            Thread.sleep(2000);
            connect();
        } catch (InterruptedException ex) {
            Logger.getLogger(WebsocketClientEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     */
    public static interface MessageHandler {

        public void handleMessage(String message);
    }
}