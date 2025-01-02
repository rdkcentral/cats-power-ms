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
import com.cats.power.model.OutletInfo;
import com.cats.power.utils.HttpClientUtil;
import com.cats.power.utils.PowerConstants;
import com.cats.power.utils.TelnetConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * SynaccessHttpPowerControllerDevice is a class that extends HttpPowerControllerDevice
 * and provides implementation for Synaccess power controller device.
 * */
public class SynaccessHttpPowerControllerDevice extends HttpPowerControllerDevice {
	private final Logger       log                   = LoggerFactory.getLogger(SynaccessHttpPowerControllerDevice.class);
	String url;
	Map<String,String> version = new HashMap<>();

	@Value("${constant.synaccessUsername}")
	private static String synaccessUsername;

	@Value("${constant.synaccessPassword}")
	private static String synaccessPassword;

	/**
	 * @param host
	 * @param port
	 */
	public SynaccessHttpPowerControllerDevice(String host, int port) {
		super(synaccessUsername, synaccessPassword);
		super.setPortIp(port);
		super.setHost(host);
		url = "http://" + host + ":" + port + "/cmd.cgi?";
		CompletableFuture.runAsync(this::getVersions);
	}
        
	/**
	 * @param host
	 * @param port
        * @param userName
        * @param password
	 */
	public SynaccessHttpPowerControllerDevice(String host, int port, String userName, String password) {
		super(userName, password);
		super.setPortIp(port);
		super.setHost(host);
		url = "http://" + host + ":" + port + "/cmd.cgi?";
		CompletableFuture.runAsync(this::getVersions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cats.power.device.PowerControllerDevice#powerOn(int)
	 * building url for transmit method
	 */
	@Override
	public boolean powerOn(int outlet) {
		boolean resp = false;
		String buildUrl = url + PowerConstants.SYNACCESS_HTTP_POWER_CODE + "%20" + outlet + "%20"
				+ PowerConstants.ON_COMMAND_HTTP_SYNACCESS;
		String response = transmit(buildUrl);
		if (response.contains("$A0")) {
			resp = true;
		}
		return resp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cats.power.device.PowerControllerDevice#powerOn(int)
	 * building url for transmit method
	 */
	@Override
	public boolean powerOff(int outlet) {
		boolean resp = false;
		String buildUrl = url + PowerConstants.SYNACCESS_HTTP_POWER_CODE + "%20" + outlet + "%20"
				+ PowerConstants.OFF_COMMAND_HTTP_SYNACCESS;
		String response = transmit(buildUrl);
		if (response.contains("$A0")) {
			resp = true;
		}
		return resp;
	}

	@Override
	public boolean powerToggle(int outlet) {
            try {
                boolean resp = false;
                String buildUrl = url + PowerConstants.SYNACCESS_HTTP_REBOOT_CODE + "%20" + outlet;
                String response = transmit(buildUrl);
                if (response.contains("$A0")) {
                    resp = true;
                }
                Thread.sleep(2100);
                return resp;
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(SynaccessHttpPowerControllerDevice.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
	}

	@Override
	public String getOutletStatus(int outlet) {
		// http://192.168.100.21/cmd.cgi?$A5 sample response :
		// $A0,11111110,0.21,0.21,XX
                return retrieveOutletStatus().get(outlet - 1).getStatus();
	}
        
        @Override
        public List<OutletInfo> retrieveOutletStatus(){
            String buildUrl = url + PowerConstants.SYNACCESS_HTTP_STATUS_CODE + "%20" + 1;
		String response = transmit(buildUrl);
                log.info("Response: " + response);
		if (response != null && response.contains("$A0")) {
			String[] responseSplit = response.split(PowerConstants.SYNACCESS_HTTP_STATUS_RESPONSE_DELIMITER);
			String allOutletStatus = responseSplit[1];
                        int outlet;
                        String outletStatus;
                        for(int i = 0; i < allOutletStatus.length(); i++){
                            outlet = i + 1;
                            char status = allOutletStatus.charAt(allOutletStatus.length() - outlet);
                            switch (Character.getNumericValue(status)) {
                                case PowerConstants.OFF_COMMAND_HTTP_SYNACCESS:
                                    outletStatus = "OFF";
                                    break;
                                case PowerConstants.ON_COMMAND_HTTP_SYNACCESS:
                                    outletStatus = "ON";
                                    break;
                                default:
                                    outletStatus = "UNKNOWN";
                                    break;
                            }
                            powerInfo.setOutletStatus(outlet, outletStatus);
                            log.debug("Outlet " + outlet + " is " + outletStatus);
                        }
		}
                return powerInfo.getOutlets();
        }

	@Override
	public Boolean powerOnAllOutlets() {
		boolean resp = false;
		String buildUrl = url + PowerConstants.SYNACCESS_HTTP_ALL_POWER_CODE + "%20"
				+ PowerConstants.ON_COMMAND_HTTP_SYNACCESS;
		String response = transmit(buildUrl);
		if (response.contains("$A0")) {
			resp = true;
		}
		return resp;
	}

	@Override
	public Boolean powerOffAllOutlets() {
		boolean resp = false;
		String buildUrl = url + PowerConstants.SYNACCESS_HTTP_ALL_POWER_CODE + "%20"
				+ PowerConstants.OFF_COMMAND_HTTP_SYNACCESS;
		String response = transmit(buildUrl);
		if (response.contains("$A0")) {
			resp = true;
		}
		return resp;
	}
	
	@Override
	public Boolean rebootAllOutlets() {
		Boolean retVal = true;
		for(int i = 1; i <= powerInfo.getNumOfOutlets(); i++){
			if(!powerToggle(i)){
				retVal = false;
			}
		}
		return retVal;
	}
        
            /**
     * @param buildUrl
     * @return response sending url as http input and returning String response
     */
    @Override
    public String transmit(String buildUrl)
    {
		HttpClientUtil httpClientUtil = CustomApplicationContext.getBean(HttpClientUtil.class);
		return httpClientUtil.transmit(buildUrl,host,portIp,POWER_DEVICE_USERNAME,POWER_DEVICE_PASSWORD);
    }

    @Override
    public Map<String , String > getVersions() {
		Map<String,String> errorVersionMap = new HashMap<>();
		errorVersionMap.put("hardware", "NA");
		errorVersionMap.put("FW", "NA");
		errorVersionMap.put("WF", "NA");
		if(!this.version.isEmpty()){
			log.info("Inside getversion method");
			return this.version;
		}
        try {
            TelnetConnection connection = new TelnetConnection(this.host, 23, ">");
            connection.connect(false);
            try {
				Thread.sleep(1000);
				if (connection.isConnected()) {
					String result = connection.sendVersionCommand("ver\r");
					Thread.sleep(1000);
					if (result != null) {
						if (result.contains("HW")) {
							result = result.replaceAll(">", "");
							Arrays.asList(result.split(" ")).forEach(word -> {
								if (word.contains("HW")) {
									this.version.put("hardware", word.split("HW")[1]);
								}
								if (word.contains("FW")) {
									this.version.put("firmware", word.split("FW")[1]);
								}
								if (word.contains("WF")) {
									this.version.put("wf", word.split("WF")[1]);
								}
							});
							//version.put("device", line.replaceAll("\n","").replaceAll("\r",""));
						}
					}
					connection.disconnect();
				}
			}
			catch (Exception e) {
				this.version = errorVersionMap;
            	log.error("Send/Receive failed");
            	if(connection.isConnected()) {
            		connection.disconnect();
				}
				return this.version;
			}
        }
        catch (Exception e) {
			this.version = errorVersionMap;
			log.error("Failed to connect to Power Device at " + this. host + " port 23");
			return this.version;
        }
        return this.version;
    }
}
