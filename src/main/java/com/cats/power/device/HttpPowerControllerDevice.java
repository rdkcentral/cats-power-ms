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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PowerControllerDevice over Http protocol
 */
public abstract class HttpPowerControllerDevice extends PowerControllerDevice

{

    public String POWER_DEVICE_USERNAME = "power.device.username";
    public String POWER_DEVICE_PASSWORD = "power.device.password";

    
    private final Logger       log                   = LoggerFactory.getLogger(HttpPowerControllerDevice.class);

    protected String           host;
    protected int              portIp;
    public HttpPowerControllerDevice(String username, String password){
        POWER_DEVICE_USERNAME = username;
        POWER_DEVICE_PASSWORD = password;
    }

    /**
     * @param buildUrl
     * @return response sending url as http input and returning String response
     */
    abstract String transmit(String buildUrl);

    @Override
    public void createPowerDevConn()
    {
        log.info("Connect {} {} {} ", this.getClass().getSimpleName());
    }

    @Override
    public void destroy()
    {
        log.info("Destroy {} {} {} ", this.getClass().getSimpleName());
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPortIp()
    {
        return portIp;
    }

    public void setPortIp(int portIp)
    {
        this.portIp = portIp;
    }
}
