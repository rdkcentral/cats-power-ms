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

import com.cats.power.device.PowerControllerDevice;
import com.cats.power.model.PowerDevice;
import com.cats.power.service.PowerDeviceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Scheduled Jobs for PowerDevice.
 * Gets information on versions and metadata of the devices.
 */
@Service
public class ScheduledJobs {

    /**
     * Map of version information.
     */
    private static Map<String , Map<String , String>> version = new HashMap<>();

    /**
     * Map of metadata information.
     */
    private static Map<String , Map<String , String>> metadata = new HashMap<>();

    /**
     * List of Power Devices.
     */
    private static List<PowerDevice> devices;

    @Autowired
    PowerDeviceManager powerDeviceManager;

    public static void setPowerDevices(List<PowerDevice> devices) {
        ScheduledJobs.devices = devices;
    }

    /**
     * Set the versions map with versions of the power devices.
     */
    public void  getVersions() {
    	
    	for(PowerDevice powerDevice : devices){
            PowerControllerDevice powerControllerDevice;
            if(powerDevice.getUserName() != null && powerDevice.getPassword() != null) {
                powerControllerDevice = powerDeviceManager.getPowerControllerDevice(powerDevice.getType(), powerDevice.getHost(), powerDevice.getPort(), powerDevice.getMaxPort(), powerDevice.getUserName(), powerDevice.getPassword());
            }else{
                powerControllerDevice = powerDeviceManager.getPowerControllerDevice(powerDevice.getType(), powerDevice.getHost(), powerDevice.getPort(), powerDevice.getMaxPort());
            }
            try {
                version.put(powerDevice.getDeviceId(), powerControllerDevice.getVersions());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            try {
                metadata.put(powerDevice.getDeviceId(), powerControllerDevice.getMetadata());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public  Map<String, Map<String,String>> getVersion() {
    		getVersions();
        return version;
    }

    public   Map<String, Map<String,String>> getMetadata() {
        	getVersions();
        return metadata;
    }

}
