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
import com.cats.power.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;


/**
 * DigitalLoggerPowerControllerDevice over Http protocol
 * This class is used to control the power outlets of a Digital Logger device
 * This class is abstract and must be extended by a class that implements the transmit method
 * */
public abstract class DigitalLoggerPowerControllerDevice extends HttpPowerControllerDevice {
    private final Logger log                   = LoggerFactory.getLogger(DigitalLoggerPowerControllerDevice.class);
    String url;
    private final int REBOOT_WAIT_TIME = 1000;
    private String password;

    @Value("${constant.digitalLoggerUsername}")
    private static String digitalLoggerUsername;

    @Value("${constant.digitalLoggerUsername}")
    private static String digitalLoggerPassword;


    /**
     * @param host
     * @param port
     */
    public DigitalLoggerPowerControllerDevice(String host, int port) {
        super(digitalLoggerUsername, digitalLoggerPassword);
        super.setPortIp(port);
        super.setHost(host);
        url = String.format("http://%s:%s", host, port);
    }
    /**
     * @param host
     * @param port
     * @param userName
     * @param password
     */
    public DigitalLoggerPowerControllerDevice(String host, int port, String userName, String password) {
        super(userName, password);
        super.setPortIp(port);
        super.setHost(host);
        url = String.format("http://%s:%s", host, port);
        this.password = password;
    }
    /*
     * (non-Javadoc)
     *
     * @see com.cats.power.device.PowerControllerDevice#powerOn(int)
     * building url for transmit method
     */
    @Override
    public boolean powerOn(int outlet) {
        String buildUrl = String.format("%s/outlet?%s=ON", url, outlet);
        transmit(buildUrl);
        try {
            //wait for digital logger ui to update status
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
        }
        if(getOutletStatus(outlet).equals("ON")){
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.cats.power.device.PowerControllerDevice#powerOn(int)
     * building url for transmit method
     */
    @Override
    public boolean powerOff(int outlet) {
        String buildUrl = String.format("%s/outlet?%s=OFF", url, outlet);
        transmit(buildUrl);
        try {
            //wait for digital logger ui to update status
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
        }
        if(getOutletStatus(outlet).equals("OFF")){
            return true;
        }
        return false;
    }

    @Override
    public boolean powerToggle(int outlet) {
        String initialState = getOutletStatus(outlet);
        String buildUrl = String.format("%s/outlet?%s=CCL", url, outlet);
        transmit(buildUrl);
        try {
            Thread.sleep(REBOOT_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(getOutletStatus(outlet).equals(initialState)) {
            return true;
        }
        return false;
    }

    @Override
    public String getOutletStatus(int outlet) {
        return retrieveOutletStatus().get(outlet - 1).getStatus();
    }

    @Override
    public Boolean powerOnAllOutlets() {
        String buildUrl = String.format("%s/outlet?a=ON", url);
        transmit(buildUrl);
        return true;

    }

    @Override
    public Boolean powerOffAllOutlets() {
        String buildUrl = String.format("%s/outlet?a=OFF", url);
        transmit(buildUrl);
        return true;
    }

    @Override
    public Boolean rebootAllOutlets() {
        String buildUrl = String.format("%s/outlet?a=CCL", url);
        transmit(buildUrl);
        return true;
    }

    /**
     * @param buildUrl
     * @return response sending url as http input and returning String response
     */
    @Override
    public String transmit(String buildUrl)
    {
        HttpClientUtil httpClientUtil = CustomApplicationContext.getBean(HttpClientUtil.class);
        return httpClientUtil.transmitWithAuth(buildUrl,host,portIp, POWER_DEVICE_USERNAME, POWER_DEVICE_PASSWORD);
    }
}
