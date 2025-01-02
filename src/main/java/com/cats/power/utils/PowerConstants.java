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

public interface PowerConstants
{
    /**
     * Scheme related constants
     */

    /**
     * Default power device port
     */
    Integer DEFAULT_PORT                        = 23;

    int     POWER_ON_SNMP_VALUE                 = 1;

    int     POWER_OFF_SNMP_VALUE                = 0;

    /**
     * Command to switch ON the device
     */
    int     ON_COMMAND_SYNACCESS                = 1;
    /**
     * Command to switch OFF the device
     */
    int     OFF_COMMAND_SYNACCESS               = 2;
    /**
     * Command to REBOOT the device
     */
    int     REBOOT_COMMAND_SYNACCESS            = 3;

    /**
     * Command to switch ON the device using HTTP
     */
    int     ON_COMMAND_HTTP_SYNACCESS           = 1;
    /**
     * Command to switch OFF the device using HTTP
     */
    int     OFF_COMMAND_HTTP_SYNACCESS          = 0;
    
    /**
     * Synaccess HTTP On/Off code. Synaccess defines sames code for On/Off but different args.
     * {@link http://synaccess-net.com//downloadDoc/NPStartup-B.pdf}
     */
    String  SYNACCESS_HTTP_POWER_CODE          = "$A3";
    /**
     * Synaccess HTTP REBOOT code
     * {@link http://synaccess-net.com//downloadDoc/NPStartup-B.pdf}
     */
    String  SYNACCESS_HTTP_REBOOT_CODE          = "$A4";
    
    /**
     * Synaccess HTTP status code
     * {@link http://synaccess-net.com//downloadDoc/NPStartup-B.pdf}
     */
    String  SYNACCESS_HTTP_STATUS_CODE          = "$A5";
    
    /**
     * Synaccess power ON/Off all ports
     */
    String  SYNACCESS_HTTP_ALL_POWER_CODE          = "$A7";
    
    String  SYNACCESS_HTTP_STATUS_RESPONSE_DELIMITER          = ",";

    /**
     * Object identifier to invoke an action on the power outlet.
     */
    String  OID_PLUG_ACTION_SYNACCESS           = "1.3.6.1.4.1.21728.3.2.1.1.4.";

    /**
     * Object identifier to get the power status of an outlet.
     */
    String  OID_PLUG_STATUS_SYNACCESS           = "1.3.6.1.4.1.21728.3.2.1.1.3.";

    String  STATUS_UNKNOWN                      = "UNKNOWN";

    String  POWER_ON                            = "ON";

    String  POWER_OFF                           = "OFF";

    Integer NUM_OUTLETS                         = 16;

    /**
     * Used for checking and/or appending to end of string.
     */
    String  NEWLINE                             = "\r\n";

}
