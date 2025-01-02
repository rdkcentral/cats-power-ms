/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import com.cats.power.exceptions.SlotMappingException;
import com.cats.power.service.PowerDeviceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * PowerOutletFactory provides a way to get a PowerOutletController for a given device and outlet.
 * */
@Service
public class PowerOutletFactory {
    private static final Logger       log               = LoggerFactory.getLogger( PowerOutletFactory.class );

    private final PowerDeviceManager powerDeviceManager;
    
    public PowerOutletFactory(PowerDeviceManager powerDeviceManager){
        this.powerDeviceManager = powerDeviceManager;
    }

    /**
     * Get a PowerOutletController for a given device and outlet.
     * @param deviceId The device id.
     * @param outlet The outlet.
     * @return The PowerOutletController.
     * @throws SlotMappingException If the device or outlet is not found.
     * */
    public PowerOutletController getPowerOutletController(String deviceId, int outlet) throws SlotMappingException {
        PowerControllerDevice device = powerDeviceManager.getPowerControllerDeviceById(deviceId);
        if(null != device){
            return new PowerOutletController(device, outlet);
        }
        log.error("No controller found for " + deviceId + ":" + outlet);
        throw new SlotMappingException("No controller found for " + deviceId + ":" + outlet);
    }
}
