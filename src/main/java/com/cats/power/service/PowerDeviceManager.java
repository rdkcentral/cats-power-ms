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

import java.net.URI;
import java.util.List;

import com.cats.power.device.PowerControllerDevice;
import com.cats.power.model.PowerDevice;
import com.cats.power.model.PowerInfo;

/**
 * This is factory for generating the power devices and getting the statistical info on the device. This factory is intended to
 * control creating, maintaining and gracefully destroying the power devices.
 * 
 */
public interface PowerDeviceManager
{

    /**
     * Create Power controller device for the following path
     * 
     * @param path
     *            - URI for which the power controller device has to be created
     * @return PowerControllerDevice object.
     */
    public PowerControllerDevice buildPowerControllerDeviceByScheme(final URI path);

    /**
     * Factory method to get power device from a known host, port and type.
     * 
     * @param type
     * @param host
     * @param port
     * @return
     */
    public PowerControllerDevice getPowerControllerDevice(String type, String host, Integer port, Integer maxPorts);
    
    public PowerControllerDevice getPowerControllerDevice(String type, String host, Integer port, Integer maxPorts, String userName, String password);

    /**
     * The method used to destroy all cached power devices
     */
    public void destroyAllControllers();

    /**
     * To remove the {@link PowerDevice}[s] associated with power controlling devices with this ip
     * 
     * @param ip
     */
    public void removePowerDevice(String ip, Integer port);
    
    /**
     * To remove the {@link PowerDevice}[s] associated with power controlling devices with this ip on the default port
     * 
     * @param ip
     */
    public void removePowerDevice(String ip);

    /**
     * Get the power info list for all the power controller devices.
     */
    public List<PowerInfo> getAllPowerDevicesInfo();
    
    public PowerInfo getPowerDeviceInfoById(String id);
    
    public PowerControllerDevice getPowerControllerDeviceById(String id);
    
    public List<PowerControllerDevice> getAllPowerDevices();
}
