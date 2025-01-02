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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PowerOutletController provides a way to control a single outlet on a PowerControllerDevice.
 */
@JsonIgnoreProperties({"powerControllerDevice"})
public class PowerOutletController {
    private final PowerControllerDevice powerControllerDevice;
    private final int outlet;
    
    public PowerOutletController(PowerControllerDevice powerControllerDevice, int outlet){
        this.powerControllerDevice = powerControllerDevice;
        this.outlet = outlet;
    }
    
    public synchronized boolean powerOn(){
        return getPowerControllerDevice().powerOn(outlet);
    }
    
    public synchronized boolean powerOff(){
        return getPowerControllerDevice().powerOff(outlet);
    }
    
    public synchronized boolean powerToggle(){
        return getPowerControllerDevice().powerToggle(outlet);
    }
    
    public String getOutletStatus(){
        return getPowerControllerDevice().getOutletStatus(outlet);
    }
    
    public int getOutlet(){
        return outlet;
    }

    /**
     * @return the powerControllerDevice
     */
    public PowerControllerDevice getPowerControllerDevice() {
        return powerControllerDevice;
    }
}
