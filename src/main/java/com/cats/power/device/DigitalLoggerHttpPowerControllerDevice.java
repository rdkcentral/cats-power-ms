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

import com.cats.power.exceptions.DeviceUnreachableException;
import com.cats.power.model.OutletInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* DigitalLoggerHttpPowerControllerDevice over Http protocol
* This class is used to control the power outlets of a Digital Logger device
* The abstract class DigitalLoggerPowerControllerDevice is extended by this class to implement the transmit method
* NOTE: Digital Logger legacy API does not perform validation against requests, even 
* failed calls will return 200 OK.
*/
public class DigitalLoggerHttpPowerControllerDevice extends DigitalLoggerPowerControllerDevice{
    private final Logger       log                   = LoggerFactory.getLogger(DigitalLoggerHttpPowerControllerDevice.class);

	/**
	 * @param host
	 * @param port
	 */
	public DigitalLoggerHttpPowerControllerDevice(String host, int port) {
		super(host, port);
	}
	/**
	 * @param host
	 * @param port
         * @param userName
         * @param password
	 */
	public DigitalLoggerHttpPowerControllerDevice(String host, int port, String userName, String password) {
		super(host, port, userName, password);
	}

	@Override
    public List<OutletInfo> retrieveOutletStatus(){
	    try{
	        String resp = "";
	        String regExp = "(?<=<div id=\"state\">).*?(?=<)";
		    String buildUrl = String.format("%s/status", url);
		    String response = transmit(buildUrl);
                
		    if (response != null) {
		        Pattern pattern = Pattern.compile(regExp);
			    Matcher matcher = pattern.matcher(response);
			    matcher.find();
			    resp = matcher.group();
		    }
		    String bitVector = String.format("%8s", Integer.toBinaryString(Integer.parseInt(resp,16)).replace(' ', '0'));
		    log.debug("Response: " + bitVector);
		    char bit;
		    int outlet;
		    String outletState;
		    for(int i = 0; i < bitVector.length(); i++){
		        outlet = i + 1;
		        try{
		            bit = bitVector.charAt(bitVector.length() - outlet);
		            if(bit == '1'){
		                outletState = "ON";
		            }
		            else{
		                outletState = "OFF";
		            }
		        }
		        catch(StringIndexOutOfBoundsException ex){
		            if(outlet <= powerInfo.getNumOfOutlets()){
		                outletState = "OFF";
		            }
		            else{
		                throw ex;
		            }
		        }
		        powerInfo.setOutletStatus(outlet, outletState);
		        log.debug("Outlet " + outlet + " is " + outletState);
		    }
		    return powerInfo.getOutlets();
	    }
	    catch(NumberFormatException | StringIndexOutOfBoundsException ex){
	        return legacyRetrieveOutletStatus();
	    }
	}
        
	private List<OutletInfo> legacyRetrieveOutletStatus(){
            
	    String resp = "";
	    String regExp = "(?<=<!-- state=).*?(?=\\s)";
	    String buildUrl = String.format("%s/index.htm", url);
	    String response = transmit(buildUrl);

	    if(response == null){
	        throw new DeviceUnreachableException("Cannot connect to device.");
	    }

	    Pattern pattern = Pattern.compile(regExp);
	    Matcher matcher = pattern.matcher(response);
	    matcher.find();
	    resp = matcher.group();

	    System.out.println("response = " + response);
	    System.out.println("resp = " + resp);
	    String bitVector = String.format("%8s", Integer.toBinaryString(Integer.parseInt(resp,16)).replace(' ', '0'));
	    if (bitVector.length() == 0){
	        throw new DeviceUnreachableException("Could not connect to digital logger");
	    }
	    log.debug("Response: " + bitVector);
	    char bit;
	    int outlet;
	    String outletState;
	    for(int i = 0; i < bitVector.length(); i++){
	        outlet = i + 1;
	        try{
	            bit = bitVector.charAt(bitVector.length() - outlet);
	            if(bit == '1'){
	                outletState = "ON";
	            }
	            else{
	                outletState = "OFF";
	            }
	        }
	        catch(StringIndexOutOfBoundsException ex){
	            if(outlet <= powerInfo.getNumOfOutlets()){
	                outletState = "OFF";
	            }
	            else{
	                throw ex;
	            }
	        }
	        powerInfo.setOutletStatus(outlet, outletState);
	        log.debug("Outlet " + outlet + " is " + outletState);
	    }
	    return powerInfo.getOutlets();
	}

	@Override
    public Map< String , String > getMetadata() {
	    Map<String, String> meta = new HashMap<>();
        String supportUrl = url + "/support.htm";
        String supportHtml = transmit(supportUrl);
        String serial = parseHtml("Serial Number", supportHtml);
        if(serial != null && !serial.equals("")) {
            meta.put("serialNumber", serial);
        }
        return meta;
    }

    @Override
    public Map<String , String > getVersions() {
        Map<String, String> versions = new HashMap<>();
        String supportUrl = url + "/support.htm";
        String supportHtml = transmit(supportUrl);
        String firmware = parseHtml("Firmware version", supportHtml);
        if(firmware != null && !firmware.equals("")) {
            versions.put("firmwareVersion", firmware);
        }
        String hardware = parseHtml("Hardware version", supportHtml);
        if(hardware != null && !hardware.equals("")) {
            versions.put("hardwareVersion", hardware);
        }
        
        return versions;
    }

    private String parseHtml(String key, String html) {
		if(html != null) {
			List<String> lines = Arrays.asList(html.split("\n"));
			boolean foundKey = false;
			for (String line : lines) {
				if (!foundKey) {
					foundKey = line.contains(key);
				} else {
					return line.replaceAll("<.+?>", "");
				}
			}
		}
	    return "";
    }
}