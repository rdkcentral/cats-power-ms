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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cats.power.device.PowerControllerDevice;
import com.cats.power.service.PowerDeviceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Power Device Health Check.
 */
@Service
public class PowerDeviceHealthCheck
{
    /**
     * List of Power Controller Devices.
     */
    List<PowerControllerDevice> powerControllerDevices;

    /**
     * Map of version information.
     */
    Map<String , Map<String , String>> version = new HashMap<>();

    /**
     * Map of metadata information.
     */
    Map<String , Map<String , String>> metadata = new HashMap<>();

    /**
     * Power Device Manager.
     */
    PowerDeviceManager powerDeviceManager;

    @Autowired
    public PowerDeviceHealthCheck(PowerDeviceManager powerDeviceManager )
    {
        this.powerDeviceManager = powerDeviceManager;
    }

    /**
     * Check the health of the power devices.
     * @return String
     * @throws Exception
     */
    public String check() throws Exception
    {
        boolean isUnhealthy = false;
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append( '[' );
        int count = 0;
        powerControllerDevices = powerDeviceManager.getAllPowerDevices();
        int size = powerControllerDevices.size();
        for ( PowerControllerDevice powerControllerDevice :  powerControllerDevices)
        {
            resultBuilder.append("{ \"deviceId\" : " + powerControllerDevice.getPowerInfo().getId() + ", \"entity\" : \"" + powerControllerDevice.getPowerInfo().getType() + "\"");


            resultBuilder.append( ", \"remarks\" : \"" );
            boolean isHttpConnectable = true;
            if(isHttpConnectable)
            {
                String healthCheckResult = powerControllerDevice.healthCheck();
                if(healthCheckResult == "HEALTHY") {
                    resultBuilder.append(" Able to check outlet status\"");
                    resultBuilder.append(", \"isHealthy\" : " + true);
                }
                else if(healthCheckResult == "NOT HEALTHY") {
                    resultBuilder.append(" Unable to check outlet status\"");
                    resultBuilder.append(", \"isHealthy\" : " + false);
                    isUnhealthy = true;
                }
                else if(healthCheckResult == "NOT IMPLEMENTED") {
                    resultBuilder.append(" Health Check not implemented\"");
                    resultBuilder.append(", \"isHealthy\" : " + true);
                }
            }
            else
            {
                isUnhealthy = true;
                resultBuilder.append( "Not reachable via http\"" );
                resultBuilder.append(", \"isHealthy\" : " + false);
            }

            resultBuilder.append(", \"host\" : \"" + powerControllerDevice.getPowerInfo().getIp() + "\"");
            Map<String,String> versions = getVersion().get(powerControllerDevice.getPowerInfo().getId());
            resultBuilder.append(", \"version\" : {");
            int versionCount = 0;
            for (String key : versions.keySet()) {
                if (versionCount !=0) {
                    resultBuilder.append(", ");
                }
                resultBuilder.append("\"" + key + "\" : \"" + versions.get(key) + "\"");
                versionCount++;
            }
            resultBuilder.append("}");

            Map<String,String> metadata = getMetadata().get(powerControllerDevice.getPowerInfo().getId());
            if(metadata.size() > 0) {
                resultBuilder.append(", \"metadata\" : {");
                int metaCount = 0;
                for (String key : metadata.keySet()) {
                    if (metaCount != 0) {
                        resultBuilder.append(", ");
                    }
                    resultBuilder.append("\"" + key + "\" : \"" + metadata.get(key) + "\"");
                    metaCount++;
                }
                resultBuilder.append("}");
            }

            count ++;
            if(count == size) {
                resultBuilder.append("}");
            }
            else {
                resultBuilder.append("},");
            }
        }
        resultBuilder.append( "]" );

        if ( isUnhealthy )
            return  resultBuilder.toString() ;
        else
            return resultBuilder.toString() ;
    }

    /**
     * Get the versions of the power devices.
     */
    public void getVersions() {
        List<PowerControllerDevice> powerControllerDevices = powerDeviceManager.getAllPowerDevices();
        for (PowerControllerDevice powerControllerDevice : powerControllerDevices) {
            try {
                version.put(powerControllerDevice.getPowerInfo().getId(), powerControllerDevice.getVersions());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                metadata.put(powerControllerDevice.getPowerInfo().getId(), powerControllerDevice.getMetadata());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Get the versions of the power devices.
     * @return Map
     */
    public  Map<String, Map<String,String>> getVersion() {
        if (null == version || version.isEmpty()) {
            getVersions();
        }
        return version;
    }

    /**
     * Get the metadata of the power devices.
     * @return Map
     */
    public   Map<String, Map<String,String>> getMetadata() {
        if (null == metadata || metadata.isEmpty()) {
            getVersions();
            }
        return metadata;
    }

}
