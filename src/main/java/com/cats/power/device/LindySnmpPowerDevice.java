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

import com.cats.power.config.CustomApplicationContext;
import com.cats.power.utils.MeasuredSnmpClientUtil;
import com.cats.power.utils.PowerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the implementation class for Lindy using SNMP commands.
 * This implementation uses the SNMP v1/v2c.
 */

public class LindySnmpPowerDevice extends SnmpPowerControllerDevice
{
	public static final int REBOOT_WAIT_PERIOD = 1000;
    /**
     * Object identifier to invoke on action of an outlet
     */
    String OID_PLUG                    = ".1.3.6.1.4.1.17420.1.2.9.1.13.0";
    /**
     * Object identifier to fetch the information of the device model
     */
    String OID_MODEL                   = ".1.3.6.1.4.1.17420.1.2.9.1.19.0";
    /**
     * Object identifier to fetch the information of device Serial info
     */
    String OID_SERIAL_NUMBER           = ".1.3.6.1.4.1.17420.1.2.3.0";
    /**
     * Object identifier to fetch the information of device Firmware version
     */
    String OID_FIRMWARE_VERSION        = ".1.3.6.1.4.1.17420.1.2.4.0";

    /**
     * PDU ID
     */
    String DEFAULT_PDU_ID                = "1";
    String OUTLET_INPUT = "";
    int default_outlet =0;

    private static final Logger log = LoggerFactory.getLogger(LindySnmpPowerDevice.class);

    public LindySnmpPowerDevice(String host, int port, int timeout, int retries) {
        super(host, port, timeout, retries);
        super.target.setVersion(SnmpConstants.version1);
    }

    public LindySnmpPowerDevice(String host)
    {
        this(host, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        super.target.setVersion(SnmpConstants.version1);

    }

    public LindySnmpPowerDevice(String host, int port)
    {
        this(host, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        super.target.setVersion(SnmpConstants.version1);
    }


    @Override
    public boolean powerOn(int outlet){
    	OUTLET_INPUT = frameOutlet(outlet, PowerConstants.POWER_ON_SNMP_VALUE, "");
    	String response = transmit(OUTLET_INPUT, OID_PLUG);
        return PowerConstants.POWER_ON.equalsIgnoreCase(parseLindySNMPResponse(response,outlet));
    }

    public boolean powerOn(String allOutlet){
    	OUTLET_INPUT = frameOutlet(default_outlet,PowerConstants.POWER_ON_SNMP_VALUE, allOutlet);
    	String response = transmit(OUTLET_INPUT, OID_PLUG);
        return PowerConstants.POWER_ON.equalsIgnoreCase(parseLindySNMPResponse(response,default_outlet));
    }

    @Override
    public boolean powerOff(int outlet) {
    	OUTLET_INPUT = frameOutlet(outlet, PowerConstants.POWER_OFF_SNMP_VALUE, "");
    	String response = transmit(OUTLET_INPUT, OID_PLUG);
        return PowerConstants.POWER_OFF.equalsIgnoreCase(parseLindySNMPResponse(response,outlet));
    }
    public boolean powerOff(String allOutlet){
    	OUTLET_INPUT = frameOutlet(default_outlet, PowerConstants.POWER_OFF_SNMP_VALUE, allOutlet);
    	String response = transmit(OUTLET_INPUT, OID_PLUG);
    	return PowerConstants.POWER_OFF.equalsIgnoreCase(parseLindySNMPResponse(response,default_outlet));
    }

    @Override
    public boolean powerToggle(int outlet) {
    	Boolean retVal = false;
		
		retVal = powerOff(outlet);
		if(retVal){
			try {
				Thread.sleep(REBOOT_WAIT_PERIOD);
			} catch (InterruptedException e) {
			}
			retVal = powerOn(outlet);
		}

		return retVal;

    }

    @Override
    public String getOutletStatus(int outlet) {
    	log.info("Lindy GetOutletStatus for"+ outlet);
    	String response = this.parseLindySNMPResponse(transmit("",OID_PLUG), outlet);
        return response;
    }
    
    @Override
  	public Boolean powerOnAllOutlets() {
  		return powerOn("ALL");
  	}

  	@Override
  	public Boolean powerOffAllOutlets() {
  		return powerOff("ALL");
  	}
  	
  	@Override
  	public Boolean rebootAllOutlets() {
  		Boolean retVal = false;
  		
  		retVal = powerOffAllOutlets();
  		if(retVal){
  			try {
  				Thread.sleep(REBOOT_WAIT_PERIOD);
  			} catch (InterruptedException e) {
  			}
  			retVal = powerOnAllOutlets();
  		}

  		return retVal;
  	}


    @Override
    public void destroy()
    {
        log.debug("Lindy SnmpPowerDevice: destroy called");
        super.destroy();
    }
    
    
    /**
     * outlets needs to be framed before  transmitting 
     * @param outlet range from 0 to 8
     * @param command represent power status of the outlet either 0 OFF or 1 ON
     * @param outletstatus
     * @return
     */
    public String frameOutlet(int outlet ,int command,String outletState) {
    	StringBuilder res = new StringBuilder();
    	int noOfoutlet = getPowerInfo().getNumOfOutlets();
    	String response = transmit("",OID_PLUG);
    	if("ALL".equalsIgnoreCase(outletState)) {
    		for (int i=0;i< noOfoutlet ;i++) {
        		res.append(command);
        		if(i != noOfoutlet -1) {
        		res.append(",");
        		}
        	}
    	}else {
    		if(null != response && !("Did not get a response".equalsIgnoreCase(response))) {
        		String [] outletStatus = response.split(",");
        		for(int i=0;i < outletStatus.length;i++) {
        			if(i == outlet-1) {
        				res.append(command);
        			}else {
        				res.append(outletStatus[i]);
        			}
        			
        			if(i != outletStatus.length-1) {
        				res.append(",");
        			}
        		}
    		}
    	}
    	return res.toString();
    }

    @Override
    protected String parseSNMPResponse(Integer response) {
    	return PowerConstants.STATUS_UNKNOWN;
    }
    
    public String parseLindySNMPResponse(String snmpResponse, int outlet) {
    	log.info("Prase lindy snmp Response " + snmpResponse);
    	if(null != snmpResponse && !("Did not get a response".equalsIgnoreCase(snmpResponse))) {
    		String [] outletStatus = snmpResponse.split(",");
    		String response ="";
    		if(outlet>0) {
    			response = outletStatus[outlet-1];
    			if (PowerConstants.POWER_ON_SNMP_VALUE == Integer.parseInt(response)) {
        			return PowerConstants.POWER_ON;
        			
        		}else {
        			return PowerConstants.POWER_OFF;
        		}
    		}else {
        		if (Arrays.stream(outletStatus)
                        .allMatch(x -> Integer.parseInt(x) != PowerConstants.POWER_OFF_SNMP_VALUE)) {
        			return PowerConstants.POWER_ON;
        			
        		}else {
        			return PowerConstants.POWER_OFF;
        		}
    		}
    	}
    	return PowerConstants.STATUS_UNKNOWN;
    }
    

    @SuppressWarnings("unchecked")
    protected String transmit( String command, String inputOid  )
    {
        String rtn = "";
        PDU request = new PDU();
        OID oid = new OID( inputOid);
        if("".equalsIgnoreCase(command)) {
        	request.setType( PDU.GET );
        	request.add( new VariableBinding( oid ) );
        	
        }else{
        	 request.setType( PDU.SET );
             request.setRequestID( new Integer32( reqId++ ) );
             request.add( new VariableBinding( oid, new OctetString(command)) );
        }
        try{
            log.info("request prior -- " +request);
                MeasuredSnmpClientUtil snmpClientUtil = CustomApplicationContext.getBean(MeasuredSnmpClientUtil.class);
                ResponseEvent responseEvent = snmpClientUtil.measuredExecute(snmp, request,target);
             if ( responseEvent != null && responseEvent.getResponse() != null ){
            	/**
            	 * for every success response we get the status of all  outlets
            	 */
            	log.info(" Lind SNMP response : " + responseEvent.getResponse().get( 0 ).getVariable().toString());
                rtn = responseEvent.getResponse().get( 0 ).getVariable().toString();
                }else{
                	rtn = "Did not get a response";
                	log.error( "Did not get a response from the power device" );
                	}
            }catch ( IOException e ){
            	log.error( "STATUS FAILED {} {} {} {} {} ERROR[{}]", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort(), rtn,
                    e.getMessage() );
            	}
        return rtn;
    }
    

    @Override
    public Map< String , String > getMetadata() {
        Map<String, String> meta = new HashMap<>();
        String model = queryOID(OID_MODEL);
        if(model != null) {
            meta.put("model", model.replaceAll("\"", ""));
        }

        String serial = queryOID(OID_SERIAL_NUMBER);
        if(serial != null) {
            meta.put("serialNumber", serial.replaceAll("\"", ""));
        }
        return meta;
    }

    @Override
    public Map<String, String > getVersions() {
        Map<String, String> versions = new HashMap<>();
        String version = queryOID(OID_FIRMWARE_VERSION);
        if(version != null) {
            versions.put("firmwareVersion", version);
        }
        return versions;
    }
    
    protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error("Thread was interrupted");
        }
    }
    

}