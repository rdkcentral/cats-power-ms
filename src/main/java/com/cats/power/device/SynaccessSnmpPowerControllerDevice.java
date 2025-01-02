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

/**
 * Synaccess SNMP Power Controller Device
 */
public class SynaccessSnmpPowerControllerDevice extends SnmpPowerControllerDevice
{

    public SynaccessSnmpPowerControllerDevice(String host, int port, int timeout, int retries)
    {
        super(host, port, timeout, retries);
    }

    public SynaccessSnmpPowerControllerDevice(String host, int port)
    {
        this(host, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);

    }

    public SynaccessSnmpPowerControllerDevice(String host)
    {
        this(host, DEFAULT_PORT);
    }

    @Override
    public boolean powerOn(int outlet)
    {
        return transmit(PowerConstants.ON_COMMAND_SYNACCESS, PowerConstants.OID_PLUG_ACTION_SYNACCESS + outlet);
    }

    @Override
    public boolean powerOff(int outlet)
    {
        return transmit(PowerConstants.OFF_COMMAND_SYNACCESS, PowerConstants.OID_PLUG_ACTION_SYNACCESS + outlet);
    }

    @Override
    public boolean powerToggle(int outlet)
    {
        return transmit(PowerConstants.REBOOT_COMMAND_SYNACCESS, PowerConstants.OID_PLUG_ACTION_SYNACCESS + outlet);
    }

    @Override
    public String getOutletStatus(int outlet)
    {
        return queryOutletStatus(PowerConstants.OID_PLUG_STATUS_SYNACCESS + outlet);
    }

	@Override
	public Boolean powerOnAllOutlets() {
		throw new UnsupportedOperationException("Power On/Off all slots is not implemented for SynaccessSnmpPowerControllerDevice");
	}

	@Override
	public Boolean powerOffAllOutlets() {
		throw new UnsupportedOperationException("Power On/Off all slots is not implemented for SynaccessSnmpPowerControllerDevice");
	}

	@Override
	public Boolean rebootAllOutlets() {
		throw new UnsupportedOperationException("rebootAllOutlets is not implemented for SynaccessSnmpPowerControllerDevice");
	}

	//Need this hack to expose endpoint for test stubbing
	public boolean transmit( int command, String oidStr ){
        return super.transmit(command, oidStr);
    }

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

    @Override
    public String healthCheck() {
        return "NOT IMPLEMENTED";
    }
}
