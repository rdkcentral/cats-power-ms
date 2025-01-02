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

import com.cats.power.model.OutletInfo;

import com.cats.power.model.PowerInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract representation of a power controller device.
 */
public abstract class PowerControllerDevice {
	public static final String OFF              = "OFF";
	public static final String ON               = "ON";
	public static final String ALL_OFF          = "ALL_OFF";
	public static final String ALL_ON           = "ALL_ON";
	public static final String ALL_REBOOT       = "ALL_REBOOT";
	public static final String BOOT             = "BOOT";
        
	protected PowerInfo powerInfo = new PowerInfo();

	/**
	 * Power on the specified outlet.
	 * 
	 * @param outlet
	 *            The outlet to power on.
	 * 
	 * @return <b>true</b> on success.
	 */
	public abstract boolean powerOn(int outlet);

	/**
	 * Power off the specified outlet.
	 * 
	 * @param outlet
	 *            The outlet to power off.
	 * 
	 * @return <b>true</b> on success.
	 */
	public abstract boolean powerOff(int outlet);

	/**
	 * Power toggle the specified outlet. Toggle off then on.
	 * 
	 * @param outlet
	 *            The outlet to power toggle.
	 * 
	 * @return <b>true</b> on success.
	 */
	public abstract boolean powerToggle(int outlet);

	/**
	 * Gets the outlet status of the specified outlet.
	 * 
	 * @param outlet
	 *            The outlet to get the status for.
	 * @return The status string.
	 */
	public abstract String getOutletStatus(int outlet);

	/**
	 * Method to initialize the power device connection.
	 */
	public abstract void createPowerDevConn();

	/**
	 * Gets a string value containing the information related to this
	 * PowerControllerDevice.
	 * 
	 * @return a string value containing the information related to this
	 *         PowerControllerDevice.
	 */
	@Override
	public String toString() {
		return powerInfo.toString();
	}

	/**
	 * Get the PowerInfo object.
	 * 
	 * @return PowerInfo object
	 */
	public PowerInfo getPowerInfo() {
		return powerInfo;
	}

	/**
	 * Set PowerInfo object.
	 * 
	 * @param pInfo
	 */
	public void setPowerInfo(PowerInfo pInfo) {
		this.powerInfo = pInfo;
	}

	/**
	 * Destroy the PowerControllerDevice. Implemented in the subclasses.
	 */
	public abstract void destroy();

	public abstract Boolean powerOnAllOutlets();
	public abstract Boolean powerOffAllOutlets();
	public abstract Boolean rebootAllOutlets();    
        
    public List<OutletInfo> retrieveOutletStatus(){
        String status;
        for(int i = 1; i <= powerInfo.getNumOfOutlets(); i++){
            status = getOutletStatus(i);
            powerInfo.setOutletStatus(i, status);
        }
        return powerInfo.getOutlets();
    }

    public String healthCheck() {
    	try {
			String status = getOutletStatus(1);
			if (status.equals("ON") || status.equals("OFF")) {
				return "HEALTHY";
			} else {
				return "NOT HEALTHY";
			}
		}
		catch (Exception e) {
    		return "NOT HEALTHY";
		}
	}

	public Map<String, String > getVersions() {
    	return new HashMap<>();
	}

	public Map<String , String > getMetadata() {
        return new HashMap<>();
    }
}