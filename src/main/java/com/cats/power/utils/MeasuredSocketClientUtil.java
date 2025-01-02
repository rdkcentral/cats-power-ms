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

import com.cats.power.service.MeasureTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

/**
 * MeasuredSocketClientUtil class sends measured commands to the server via Web Socket.
 * */
@Component
@Slf4j
public class MeasuredSocketClientUtil {

    /**
     * Sends a command to the server.
     * @param outToServer The output stream to the server.
     * @param cmd The command to send.
     * @param echo Whether to echo the command.
     * @return Whether the command was sent.
     * */
    @MeasureTime
    public boolean sendCmd(OutputStream outToServer, String cmd, final boolean echo) {
        if (!cmd.endsWith(PowerConstants.NEWLINE)) {
            cmd += PowerConstants.NEWLINE;
        }
        try {
            if (null != outToServer) {
                outToServer.write(cmd.getBytes());
                if (echo) {
                    log.info("Writing to : ["
                            + cmd.replaceAll("(\\r|\\n)", "") + "]");
                }
                outToServer.flush();
                return true;
            }
        } catch (IOException ioe) {
            log.error("IOException: " + ioe.getMessage());
        }
        return false;
    }



}

