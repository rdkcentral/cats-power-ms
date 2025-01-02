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
import org.snmp4j.CommunityTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the implementation class for Eaton G3 using SNMP commands.
 * This implementation uses the SNMP v1/v2c.
 */

public class RaritanSnmpPowerDevice extends SnmpPowerControllerDevice
{
    /**
     * Object identifier to invoke on action of an outlet
     */
    String OID_PLUG_ON                    = ".1.3.6.1.4.1.13742.6.4.1.2.1.2.";

    /**
     * Object identifier to invoke off action of an outlet
     */
    String OID_PLUG_OFF                  = ".1.3.6.1.4.1.13742.6.4.1.2.1.2.";

    /**
     * Object identifier to get status of an outlet
     */
    String OID_PLUG_STATUS                = ".1.3.6.1.4.1.13742.6.4.1.2.1.3.";

    /**
     * Object identifier to cycle the outlet
     */
    String OID_PLUG_REBOOT                = ".1.3.6.1.4.1.13742.6.4.1.2.1.2.";

    String OID_MODEL                      = ".1.3.6.1.4.1.13742.6.3.2.1.1.3.";
    String OID_SERIAL_NUMBER              = ".1.3.6.1.4.1.13742.6.3.2.1.1.4.";
    String OID_FIRMWARE_VERSION              = ".1.3.6.1.4.1.13742.6.3.2.3.1.6.1.1.";

    /**
     * PDU ID
     */
    String DEFAULT_PDU_ID                = "1";

    /**
     * PDU ID
     */
    Integer OFF_COMMAND               = 0;

    /**
     * PDU ID
     */
    Integer ON_COMMAND                = 1;

    /**
     * PDU ID
     */
    Integer CYCLE_COMMAND             = 2;

    String pduId = DEFAULT_PDU_ID;

    CommunityTarget writeTarget; //Raritan have different communities fo read and write;


    private static final Logger log = LoggerFactory.getLogger(RaritanSnmpPowerDevice.class);

    public RaritanSnmpPowerDevice(String host, int port, int timeout, int retries) {
        super(host, port, timeout, retries);
        super.target.setVersion(SnmpConstants.version1);

        writeTarget = new CommunityTarget();
        writeTarget.setAddress( target.getAddress() );
        writeTarget.setTimeout( target.getTimeout() );
        writeTarget.setRetries( target.getRetries() );
        writeTarget.setCommunity( new OctetString( "private" ) );
        writeTarget.setVersion( SnmpConstants.version2c );
    }

    public RaritanSnmpPowerDevice(String host, int port, int timeout, int retries, String pduId) {

        this(host, port, timeout, retries);
        if(pduId == null || pduId.isEmpty()){
            throw new IllegalArgumentException("PDUID cannot be null or empty");
        }
        this.pduId = pduId;
    }

    public RaritanSnmpPowerDevice(String host)
    {
        this(host, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        super.target.setVersion(SnmpConstants.version1);

    }

    public RaritanSnmpPowerDevice(String host, int port)
    {
        this(host, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        super.target.setVersion(SnmpConstants.version1);
    }


    @Override
    public boolean powerOn(int outlet){

        return transmit(writeTarget, ON_COMMAND, getOidStringForOutlet(OID_PLUG_ON, outlet));
    }

    @Override
    public boolean powerOff(int outlet) {

        return transmit(writeTarget, OFF_COMMAND, getOidStringForOutlet(OID_PLUG_OFF, outlet));
    }

    @Override
    public boolean powerToggle(int outlet) {

        return transmit(writeTarget, CYCLE_COMMAND, getOidStringForOutlet(OID_PLUG_REBOOT, outlet));
    }

    @Override
    public String getOutletStatus(int outlet) {
        return super.queryOutletStatus(OID_PLUG_STATUS+pduId+"."+outlet);
    }

    @Override
    public void destroy()
    {
        log.debug("RaritanSnmpPowerDevice: destroy called");
        super.destroy();
    }

    @Override
    protected String parseSNMPResponse(Integer response) {
        if ( response == 7 )
        {
            return ON;
        }
        else if ( response == 8 )
        {
            return OFF;
        }

        return PowerConstants.STATUS_UNKNOWN;
    }

    @Override
	public Boolean powerOnAllOutlets() {
		boolean retVal = true;
		// There is no interface to send powerOn to all ports via SNMP
		// so send one by one.
        if(getPowerInfo().getNumOfOutlets() < 0 ){
            throw new IllegalArgumentException("Raritan max ports is not defined "+getPowerInfo().getNumOfOutlets());
        }
		for(int i = 1; i <= getPowerInfo().getNumOfOutlets(); i++){
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
        if(getPowerInfo().getNumOfOutlets() < 0 ){
            throw new IllegalArgumentException("Raritan max ports is not defined "+getPowerInfo().getNumOfOutlets());
        }
        for(int i = 1; i <= getPowerInfo().getNumOfOutlets(); i++){
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
        if(getPowerInfo().getNumOfOutlets() < 0 ){
            throw new IllegalArgumentException("Raritan max ports is not defined "+getPowerInfo().getNumOfOutlets());
        }
        for(int i = 1; i <= getPowerInfo().getNumOfOutlets(); i++){
			boolean isSuccess = powerToggle(i);
			if(!isSuccess){
				retVal = false;
			}
		}
		return retVal;
	}

	public boolean transmit(CommunityTarget target, Integer command, String oidStr ){
        return super.transmit(target, command, oidStr);
    }

    private String getOidStringForOutlet(String oid, Integer outlet){
        return oid+pduId+"."+outlet;
    }

    @Override
    public Map< String , String > getMetadata() {
        Map<String, String> meta = new HashMap<>();
        String model = queryOID(OID_MODEL+pduId);
        if(model != null) {
            meta.put("model", model.replaceAll("\"", ""));
        }

        String serial = queryOID(OID_SERIAL_NUMBER+pduId);
        if(serial != null) {
            meta.put("serialNumber", serial.replaceAll("\"", ""));
        }
        return meta;
    }

    @Override
    public Map<String, String > getVersions() {
        Map<String, String> versions = new HashMap<>();
        String version = queryOID(OID_FIRMWARE_VERSION+pduId);
        if(version != null) {
            versions.put("firmwareVersion", version);
        }
        return versions;
    }

}