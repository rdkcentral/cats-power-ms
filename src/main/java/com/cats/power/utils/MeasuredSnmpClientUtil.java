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
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MeasuredSnmpClientUtil class sends measured commands to the server via SNMP.
 * */
@Component
@Slf4j
public class MeasuredSnmpClientUtil {

    /**
     * Sends a command to the server.
     * @param snmp The SNMP client.
     * @param request The PDU request.
     * @param target The target.
     * @return The response event.
     * */
    @MeasureTime
    public ResponseEvent measuredExecute(Snmp snmp, PDU request, CommunityTarget target) throws IOException {
         return  snmp.send( request, target );
    }

}
