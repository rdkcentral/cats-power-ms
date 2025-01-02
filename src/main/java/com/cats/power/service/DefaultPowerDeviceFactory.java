package com.cats.power.service;

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

import com.cats.power.device.*;
import com.cats.power.model.OutletInfo;
import com.cats.power.model.PowerInfo;

import java.util.ArrayList;
import com.cats.power.utils.PowerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default factory implementation, this class needs to be singleton
 * 
 */
@Service
public class DefaultPowerDeviceFactory
{
    
    @Value("${constant.synaccessUsername}")
    private String synaccessUsername;
    
    @Value("${constant.synaccessPassword}")
    private String synaccessPassword;
    
    @Value("${constant.digitalLoggerUsername}")
    private String digitalLoggerUsername;
    
    @Value("${constant.digitalLoggerUsername}")
    private String digitalLoggerPassword;

    Logger log = LoggerFactory.getLogger(DefaultPowerDeviceFactory.class);

    public PowerControllerDevice buildPowerController(String scheme, String ip, Integer port, Integer maxPorts)
    {
        PowerControllerDevice powerControllerDevice;

        PowerDeviceType powerDeviceType = PowerDeviceType.findType(scheme);

        switch (powerDeviceType)
        {
            case EATON_G3:
                powerControllerDevice = new Eaton_G3_SNMPPowerDevice(ip);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.EATON_G3.getScheme());
                break;
            case DIGITAL_LOGGER:
                powerControllerDevice = new DigitalLoggerHttpPowerControllerDevice(ip, port, digitalLoggerUsername, digitalLoggerPassword);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.DIGITAL_LOGGER.getScheme());
                break;
            case DIGITAL_LOGGER_REST:
                powerControllerDevice = new DigitalLoggerRestHttpPowerControllerDevice(ip, port, digitalLoggerUsername, digitalLoggerPassword);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.DIGITAL_LOGGER.getScheme());
                break;
            case RARITAN:
                powerControllerDevice = new RaritanSnmpPowerDevice(ip, port);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.RARITAN.getScheme());
                break;
            case LINDY:
                powerControllerDevice = new LindySnmpPowerDevice(ip, port);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.LINDY.getScheme());
                break;
            case SYNACCESS:
            default:
                powerControllerDevice = new SynaccessHttpPowerControllerDevice(ip, port, synaccessUsername, synaccessPassword);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.SYNACCESS.getScheme());
                break;
        }
        setPowerDeviceProperties(powerControllerDevice, ip, port, maxPorts);
        return powerControllerDevice;
    }

    public PowerControllerDevice buildPowerController(String scheme, String ip, Integer port, Integer maxPorts, String userName, String password)
    {
        PowerControllerDevice powerControllerDevice;

        PowerDeviceType powerDeviceType = PowerDeviceType.findType(scheme);

        switch (powerDeviceType)
        {
            case EATON_G3:
                powerControllerDevice = new Eaton_G3_SNMPPowerDevice(ip);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.EATON_G3.getScheme());
                break;
            case DIGITAL_LOGGER:
                powerControllerDevice = new DigitalLoggerHttpPowerControllerDevice(ip, port, userName, password);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.DIGITAL_LOGGER.getScheme());
                break;
            case DIGITAL_LOGGER_REST:
                powerControllerDevice = new DigitalLoggerRestHttpPowerControllerDevice(ip, port, userName, password);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.DIGITAL_LOGGER.getScheme());
                break;
            case SYNACCESS:
            default:
                powerControllerDevice = new SynaccessHttpPowerControllerDevice(ip, port, userName, password);
                powerControllerDevice.getPowerInfo().setScheme(PowerDeviceType.SYNACCESS.getScheme());
                break;
        }
        setPowerDeviceProperties(powerControllerDevice, ip, port, maxPorts);
        return powerControllerDevice;
    }
    
    private void setPowerDeviceProperties(PowerControllerDevice powerControllerDevice, String ip, Integer port, Integer maxPorts)
    {
        log.info("Setting properties ip: " + ip + " port: " + port + " maxports: " + maxPorts);
        if (powerControllerDevice != null)
        {
            port = port < 0 ? PowerConstants.DEFAULT_PORT : port;
            powerControllerDevice.getPowerInfo().setPort(port);
            powerControllerDevice.getPowerInfo().setIp(ip);
            if(maxPorts == null || maxPorts < 0){
            	maxPorts = PowerConstants.NUM_OUTLETS;
            }
            powerControllerDevice.getPowerInfo().setNumOfOutlets(maxPorts);
            powerControllerDevice.createPowerDevConn();
            
            PowerInfo powerInfo = new PowerInfo(powerControllerDevice.getPowerInfo().getScheme(), ip, port);
            powerInfo.setNumOfOutlets(maxPorts);
            List<OutletInfo> outlets = new ArrayList<>();
            for(int i = 1; i <= maxPorts; i++){
                OutletInfo outletInfo = new OutletInfo();
                outletInfo.setOutlet((i));
                outletInfo.setStatus("UNKNOWN");
                outlets.add(outletInfo);
            }
            powerInfo.setOutlets(outlets);
            powerControllerDevice.setPowerInfo(powerInfo);
        }
        else
        {
            log.info(" PowerControllerDevice is NULL, may be caused due to pre existing errors");
        }
    }
}
