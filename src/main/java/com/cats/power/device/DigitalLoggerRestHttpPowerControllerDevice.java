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
import com.cats.power.model.DigitalLoggerOutletInfo;
import com.cats.power.model.OutletInfo;
import com.cats.power.model.PowerInfo;
import com.cats.power.utils.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * DigitalLoggerRestHttpPowerControllerDevice is an implementation of the PowerControllerDevice
 * interface for the Digital Logger power controller device.
 * */
public class DigitalLoggerRestHttpPowerControllerDevice extends DigitalLoggerPowerControllerDevice{

    private final Logger log                   = LoggerFactory.getLogger(DigitalLoggerHttpPowerControllerDevice.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final int REBOOT_WAIT_TIME = 1000;

    /**
     * @param host
     * @param port
     */
    public DigitalLoggerRestHttpPowerControllerDevice(String host, int port) {
        super(host, port);
    }
    /**
     * @param host
     * @param port
     * @param userName
     * @param password
     */
    public DigitalLoggerRestHttpPowerControllerDevice(String host, int port, String userName, String password) {
        super(host, port, userName, password);
    }

    @Override
    public List<OutletInfo> retrieveOutletStatus(){
        long start = System.currentTimeMillis();
        String buildUrl = String.format("%s/restapi/relay/outlets/", url);
        String response = transmit(buildUrl);
        log.info("Got response in: " + (System.currentTimeMillis() - start));
        try{
            List<DigitalLoggerOutletInfo> outletInfo = mapper.readValue(response, mapper.getTypeFactory().constructCollectionType(List.class, DigitalLoggerOutletInfo.class));

            log.info("Parsed response in: " + (System.currentTimeMillis() - start));
            outletInfo.stream().forEach(outlet -> {
                int outletNum;
                String outletState = "UNKNOWN";
                String outletName = outlet.getName();
                String[] parts = outletName.split("\\s+");
                outletNum = Integer.parseInt(parts[1]);
                if(outlet.getState()){
                    outletState = "ON";
                }
                else if (!outlet.getState()){
                    outletState = "OFF";
                }
                powerInfo.setOutletStatus(outletNum, outletState);
                log.debug("Outlet " + outletNum + " is " + outletState);
            });
            log.info("updated info in: " + (System.currentTimeMillis() - start));
        }catch(IOException | NumberFormatException ex){
            log.error("Could not parse digital logger REST response: " + ex.getLocalizedMessage());
        }

        log.info("Returning in response in: " + (System.currentTimeMillis() - start));
        return powerInfo.getOutlets();
    }

    @Override
    public Map< String,String > getVersions() {
        Map<String, String> versions = new HashMap<>();
        String coreFirmwareUrl = url + "/restapi/relay/version";
        String coreFirmware = transmit(coreFirmwareUrl);
        if(coreFirmware != null) {
            versions.put("coreFirmware", coreFirmware.replaceAll("\"", ""));
        }
        String frontEndFirmwareUrl = url + "/restapi/config/version";
        String frontEndFirmware = transmit(frontEndFirmwareUrl);
        if(frontEndFirmware != null) {
            versions.put("frontEndFirmware", frontEndFirmware.replaceAll("\"", ""));
        }
        return versions;
    }

    @Override
    public Map< String , String > getMetadata() {
        Map<String, String> meta = new HashMap<>();
        String modelUrl = url + "/restapi/config/hardware_id";
        String model = transmit(modelUrl);
        if(model != null) {
            meta.put("model", model.replaceAll("\"", ""));
        }
        String serialUrl = url + "/restapi/config/serial";
        String serial = transmit(serialUrl);
        if(serial != null) {
            meta.put("serialNumber", serial.replaceAll("\"", ""));
        }
        return meta;
    }

    @Override
    public boolean powerOn(int outlet){
        String buildUrl = String.format("%s/restapi/relay/outlets/%s/state/", url, outlet);
        HttpPut httpPut = new HttpPut(buildUrl);

        try {
            StringEntity stringEntity = new StringEntity("true");
            httpPut.setEntity(stringEntity);
            httpPut.setHeader("X-CSRF", "x");
            String response = transmit(httpPut);

            TimeUnit.SECONDS.sleep(1);
            log.info("Power ON Response: {}", response);
        } catch (UnsupportedEncodingException | InterruptedException e) {
            e.printStackTrace();
            log.warn("Caught exception while powering outlet on: {}", e);
        }

        return getOutletStatus(outlet).equals("true");
    }

    @Override
    public boolean powerOff(int outlet){
        String buildUrl = String.format("%s/restapi/relay/outlets/%s/state/", url, outlet);
        HttpPut httpPut = new HttpPut(buildUrl);

        try {
            StringEntity stringEntity = new StringEntity("false");
            httpPut.setEntity(stringEntity);
            httpPut.setHeader("X-CSRF", "x");
            String response = transmit(httpPut);
            log.info("RESPONSE: {}", response);

            TimeUnit.SECONDS.sleep(1);
            log.info("Power OFF Response: {}", response);
        } catch(UnsupportedEncodingException | InterruptedException e){
            log.warn("Caught exception while powering outlet off: {}", e.getMessage());
        }

        return getOutletStatus(outlet).equals("false");
    }

    @Override
    public boolean powerToggle(int outlet){
        String initialState = getOutletStatus(outlet);

        String buildUrl = String.format("%s/restapi/relay/outlets/%s/cycle/", url, outlet);
        HttpPost httpPost = new HttpPost(buildUrl);

        try {
            httpPost.setHeader("X-CSRF", "x");
            String response = transmit(httpPost);

            Thread.sleep(REBOOT_WAIT_TIME);
            log.info("Power Toggle Response: {}", response);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.warn("Caught exception while power toggling outlet: {}", e.getMessage());
        }

        return getOutletStatus(outlet).equals(initialState);
    }

    @Override
    public String getOutletStatus(int outlet){
        long startTime = System.currentTimeMillis();
        String buildUrl = String.format("%s/restapi/relay/outlets/%s/physical_state/", url, outlet);

        try {
            String response = transmit(buildUrl);
            log.info("Got response in: " + (System.currentTimeMillis() - startTime));
            return response;
        }catch(Exception e){
            log.warn("Caught exception while getting outlet status: {}", e.getMessage());
            throw e;
        }
    }

    public String getAllOutletStatuses() {
        long startTime = System.currentTimeMillis();
        String buildUrl = String.format("%s/restapi/relay/outlets/all;/physical_state/", url);

        String response = transmit(buildUrl);

        log.info("Get All Outlet Statuses Response: {}", response);
        log.info("Got response in: " + (System.currentTimeMillis() - startTime));

        return response;
    }

    public String getExpectedOutletStatusResponse(PowerInfo powerInfo, String expectedString){
        String expectedOutletStatusResponse = "[";
        for(int i = 0; i < powerInfo.getNumOfOutlets(); i++){
            if (i == powerInfo.getNumOfOutlets() -1) {
                expectedOutletStatusResponse = expectedOutletStatusResponse.concat(expectedString);
            } else {
                expectedOutletStatusResponse = expectedOutletStatusResponse.concat(expectedString.concat(","));
            }
        }

        return expectedOutletStatusResponse.concat("]");
    }

    @Override
    public Boolean powerOnAllOutlets(){
        String onStatus = getExpectedOutletStatusResponse(this.powerInfo, "true");
        log.info("EXPECTED: {} ", onStatus);
        String buildUrl = String.format("%s/restapi/relay/outlets/all;/state/", url);
        HttpPut httpPut = new HttpPut(buildUrl);

        try {
            StringEntity stringEntity = new StringEntity("true");
            httpPut.setEntity(stringEntity);
            httpPut.setHeader("X-CSRF", "x");
            String response = transmit(httpPut);

            TimeUnit.SECONDS.sleep(1);
            log.info("Power ON All Outlets Response: {}", response);
        } catch(UnsupportedEncodingException | InterruptedException e){
            log.warn("Caught exception while powering all outlets ON: {}", e.getMessage());
        }
        log.info("RESPONSE: {}", getAllOutletStatuses());

        return getAllOutletStatuses().equals(onStatus);
    }

    @Override
    public Boolean powerOffAllOutlets(){
        String offStatus = getExpectedOutletStatusResponse(this.powerInfo, "false");
        String buildUrl = String.format("%s/restapi/relay/outlets/all;/state/", url);
        HttpPut httpPut = new HttpPut(buildUrl);

        try {
            StringEntity stringEntity = new StringEntity("false");
            httpPut.setEntity(stringEntity);
            httpPut.setHeader("X-CSRF", "x");
            String response = transmit(httpPut);

            TimeUnit.SECONDS.sleep(1);
            log.info("Power OFF All Outlets Response: {}", response);
        } catch(InterruptedException | UnsupportedEncodingException e){
            log.warn("Caught exception while powering all outlets OFF: {}", e.getMessage());
        }

        return getAllOutletStatuses().equals(offStatus);
    }

    @Override
    public Boolean rebootAllOutlets(){
        String buildUrl = String.format("%s/restapi/relay/outlets/all;/cycle/", url);
        HttpPost httpPost = new HttpPost(buildUrl);

        try {
            httpPost.setHeader("X-CSRF", "x");
            String response = transmit(httpPost);
            log.info("Reboot response: {}", response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String transmit(HttpRequestBase httpRequestBase) {

        byte[] encodedCredentials = Base64.getEncoder().encode((String.format("%s:%s", POWER_DEVICE_USERNAME, POWER_DEVICE_PASSWORD)).getBytes());
        String headerCredentials = String.format("Basic %s", new String(encodedCredentials));
        Header header = new BasicHeader("authorization", headerCredentials);
        httpRequestBase.setHeader(header);

        HttpClientUtil httpClientUtil = CustomApplicationContext.getBean(HttpClientUtil.class);
        return httpClientUtil.transmit(httpRequestBase, host, portIp, POWER_DEVICE_USERNAME, POWER_DEVICE_PASSWORD);}

}
