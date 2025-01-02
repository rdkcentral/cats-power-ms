package com.cats.power.device;

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

import com.cats.power.utils.PowerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;

/**
 * This is the implementation class for Eaton G3 using SNMP commands.
 * This implementation uses the SNMP v1/v2c.
 */

public class Eaton_G3_SNMPPowerDevice extends SnmpPowerControllerDevice
{
    /**
     * Object identifier to invoke on action of an outlet
     */
    String OID_PLUG_ON_EATON                    = ".1.3.6.1.4.1.534.6.6.7.6.6.1.4.0.";
    
    /**
     * Object identifier to invoke off action of an outlet
     */
    String OID_PLUG_OFF_EATON                   = ".1.3.6.1.4.1.534.6.6.7.6.6.1.3.0.";
    
    /**
     * Object identifier to get status of an outlet
     */
    String OID_PLUG_STATUS_EATON                = ".1.3.6.1.4.1.534.6.6.7.6.6.1.2.0.";

    /**
     * Object identifier to get status of an outlet
     */
    String OID_PLUG_REBOOT_EATON                = ".1.3.6.1.4.1.534.6.6.7.6.6.1.5.0.";
    
    public static final String[] EATON_G3_OUTLET_LIST = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11",
			"12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" };
    
    /**
     * Command to change power state of the device
     */
    int     POWER_COMMAND_EATON                 = 1;
    
    private static final Logger log = LoggerFactory.getLogger(Eaton_G3_SNMPPowerDevice.class);

    public Eaton_G3_SNMPPowerDevice(String host, int port, int timeout, int retries)
    {
        super(host, port, timeout, retries);
        super.target.setVersion(SnmpConstants.version1);
    }
    
    public Eaton_G3_SNMPPowerDevice(String host)
    {
        this(host, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        super.target.setVersion(SnmpConstants.version1);

    }

    public Eaton_G3_SNMPPowerDevice(String host, int port)
    {
        this(host, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        super.target.setVersion(SnmpConstants.version1);
    }


    @Override
    public boolean powerOn(int outlet)
    {
        return transmit(POWER_COMMAND_EATON, OID_PLUG_ON_EATON + outlet);
    }

    @Override
    public boolean powerOff(int outlet)
    {
        return transmit(POWER_COMMAND_EATON, OID_PLUG_OFF_EATON + outlet);
    }

    @Override
    public boolean powerToggle(int outlet)
    {
        return transmit(POWER_COMMAND_EATON, OID_PLUG_REBOOT_EATON + outlet);
    }

    @Override
    public String getOutletStatus(int outlet)
    {
        return super.queryOutletStatus(OID_PLUG_STATUS_EATON + outlet);
    }

    @Override
    public void destroy()
    {
        log.debug("Eaton_G3_SNMPPowerDevice: destroy called");
        super.destroy();
    }

	@Override
	public Boolean powerOnAllOutlets() {
		boolean retVal = true;
		// There is no interface to send powerOn to all ports via SNMP
		// so send one by one.
		for(int i = 1; i <= EATON_G3_OUTLET_LIST.length; i++){
			boolean isSuccess = powerOn(i);
			if(!isSuccess){
				retVal = false;
			}
		}
		return retVal;
	}

	@Override
	public Boolean powerOffAllOutlets() {
		boolean retVal = true;
		// There is no interface to send powerOn to all ports via SNMP
		// so send one by one.
		for(int i = 1; i <= EATON_G3_OUTLET_LIST.length; i++){
			boolean isSuccess = powerOff(i);
			if(!isSuccess){
				retVal = false;
			}
		}
		return retVal;
	}
	
	@Override
	public Boolean rebootAllOutlets() {
		boolean retVal = true;
		// There is no interface to send powerOn to all ports via SNMP
		// so send one by one.
		for(int i = 1; i <= EATON_G3_OUTLET_LIST.length; i++){
			boolean isSuccess = powerToggle(i);
			if(!isSuccess){
				retVal = false;
			}
		}
		return retVal;
	}

	public boolean transmit( int command, String oidStr ){ return super.transmit(command, oidStr); }

    @Override
    protected String parseSNMPResponse(Integer response) {
        if ( response == 1 )
        {
            return ON;
        }
        else if ( response == 0 )
        {
            return OFF;
        }

        return PowerConstants.STATUS_UNKNOWN;
    }


}