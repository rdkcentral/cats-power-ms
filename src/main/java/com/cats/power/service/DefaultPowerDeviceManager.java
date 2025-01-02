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

import com.cats.power.config.PowerConfiguration;
import com.cats.power.device.PowerControllerDevice;
import com.cats.power.exceptions.DeviceUnreachableException;
import com.cats.power.model.PowerDevice;
import com.cats.power.model.PowerInfo;
import com.cats.power.utils.PowerConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DefaultPowerDeviceManager supports default power device operations
 * and allows for managing power devices configured on the rack.
 * */
@Service
public class DefaultPowerDeviceManager implements PowerDeviceManager{
    protected Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    DefaultPowerDeviceFactory deviceFactory;
    private List<PowerControllerDevice> powerDevices = new ArrayList<>();
    @Autowired
    public PowerConfiguration powerConfig;

	@Override
    public PowerControllerDevice getPowerControllerDevice(String type, String ip, Integer port, Integer maxPorts)
    {
        PowerControllerDevice powerDevice = null;
        try{
            powerDevice = powerDevices.stream()
                .filter(device -> device.getPowerInfo().getIp().equals(ip) && device.getPowerInfo().getPort() == port)
                .findFirst().get();
            log.info(String.format("Power device at %s:%s found, returning", ip, port));
            return powerDevice;
        }
        catch(NoSuchElementException ex){
            log.info(String.format("Power device at %s:%s not found, creating", ip, port));
        }
        try
        {
            String uriString = String.format("%s://%s:%s?maxPort=%s", type, ip, port, maxPorts);
            URI uri = new URI(uriString);
            powerDevice = buildPowerControllerDeviceByScheme(uri);
        }
        catch (URISyntaxException e)
        {
            log.error("getPowerDevice excpetion. URI is not proper " + e.getMessage());
        }
        return powerDevice;
    }
    
    @Override
    public PowerControllerDevice getPowerControllerDevice(String type, String ip, Integer port, Integer maxPorts, String userName, String password)
    {
        PowerControllerDevice powerDevice = null;
        try{
            powerDevice = powerDevices.stream()
                .filter(device -> device.getPowerInfo().getIp().equals(ip) && device.getPowerInfo().getPort() == port)
                .findFirst().get();
            log.info(String.format("Power device at %s:%s found, returning", ip, port));
            return powerDevice;
        }
        catch(NoSuchElementException ex){
            log.info(String.format("Power device at %s:%s not found, creating", ip, port));
        }
        try
        {
            String uriString = String.format("%s://%s:%s?maxPort=%s&userName=%s&password=%s", type, ip, port, maxPorts, userName, password);
            URI uri = new URI(uriString);
            powerDevice = buildPowerControllerDeviceByScheme(uri);
        }
        catch (URISyntaxException e)
        {
            log.error("getPowerDevice excpetion. URI is not proper " + e.getMessage());
        }
        return powerDevice;
    }
    
    @Override
    public void destroyAllControllers()
    {
        powerDevices.stream().forEach(device -> device.destroy());
        powerDevices = new ArrayList<>();
        log.debug("Destroyed all devices in cache ...");
    }

    @Override
    public void removePowerDevice(String ip)
    {
    	removePowerDevice(ip, PowerConstants.DEFAULT_PORT);
    }
    
    @Override
    public void removePowerDevice(String ip, Integer port) 
    {
        try {
            String uriString = String.format("http://%s:%s/", ip, port);
            URI path;
            path = new URI(uriString);
	    PowerControllerDevice device = buildPowerControllerDeviceByScheme(path);
            device.destroy();
            powerDevices.remove(device);
        } catch (URISyntaxException ex) {
            log.error("Could not convert ip and port into valid URI: " + ip + ":" + port);
        }
        log.debug("Remove device with ip {} ", ip);
    }

    @Override
    public ArrayList<PowerInfo> getAllPowerDevicesInfo()
    {
    	ArrayList<PowerInfo> powerInfoList;
    	if(null == powerDevices || powerDevices.size()==0) {
    		this.getAllPowerDevices();
            powerInfoList = powerDevices.stream().map(PowerControllerDevice::getPowerInfo).collect(Collectors.toCollection(ArrayList::new));
    	}else {
            powerInfoList = powerDevices.stream().map(PowerControllerDevice::getPowerInfo).collect(Collectors.toCollection(ArrayList::new));
    	}
        log.debug("collected info for {} devices " + powerInfoList.size());
        return powerInfoList;
    }
    
    @Override
    public PowerInfo getPowerDeviceInfoById(String id)
    {
        return getPowerControllerDeviceById(id).getPowerInfo();
    }
    @Override
    public PowerControllerDevice buildPowerControllerDeviceByScheme(URI path)
    {   
        String scheme = path.getScheme();
        PowerControllerDevice powerDevice;
        List<NameValuePair> params = URLEncodedUtils.parse(path, "UTF-8");
        Integer maxPorts = null;
        String userName = null;
        String password = null;
        for (NameValuePair param : params) {
        	if(param.getName().equals("maxPort")){
        		maxPorts = Integer.parseInt(param.getValue());
        	}
                else if(param.getName().equals("userName")){
        		userName = param.getValue();
        	}
                else if(param.getName().equals("password")){
        		password = param.getValue();
        	}
        }
        if(userName != null && password != null){
            powerDevice = deviceFactory.buildPowerController(scheme, path.getHost(), path.getPort(), maxPorts, userName, password);
        }
        else{
            powerDevice = deviceFactory.buildPowerController(scheme, path.getHost(), path.getPort(), maxPorts);
        }
        try {
        powerDevice.retrieveOutletStatus();
        }catch(Exception e) {
        	log.error("Could not retrieve the device status: " + e.getMessage());
        }
        return powerDevice;
    }
    
    @Override
    public PowerControllerDevice getPowerControllerDeviceById(String id){
        PowerControllerDevice powerDevice = null;
        try{
        	if(null == powerDevices || powerDevices.size()==0) {
        		this.getAllPowerDevices();
        		powerDevice =  powerDevices.stream()
                        .filter(device -> device.getPowerInfo().getId().equals(id))
                        .findFirst()
                        .get();
        	}else {
        		powerDevice =  powerDevices.stream()
                        .filter(device -> device.getPowerInfo().getId().equals(id))
                        .findFirst()
                        .get();
        	}
            
        }
        catch(NoSuchElementException ex){
        
        }
        if(null == powerDevice) {
        	throw new DeviceUnreachableException("Cannot connect to device." +id);
        }
        return powerDevice;
    }
    
    @Override
    public synchronized List<PowerControllerDevice> getAllPowerDevices(){
    	
    	if(powerDevices.isEmpty()) {
    	for(PowerDevice powerDevice : powerConfig.getPowerDevices()){
            PowerControllerDevice powerControllerDevice;
            if(powerDevice.getUserName() != null && powerDevice.getPassword() != null) {
                powerControllerDevice = this.getPowerControllerDevice(powerDevice.getType(), powerDevice.getHost(), powerDevice.getPort(), powerDevice.getMaxPort(), powerDevice.getUserName(), powerDevice.getPassword());
            }else{
                powerControllerDevice = this.getPowerControllerDevice(powerDevice.getType(), powerDevice.getHost(), powerDevice.getPort(), powerDevice.getMaxPort());
            }
            powerControllerDevice.getPowerInfo().setId(powerDevice.getDeviceId());
            powerDevices.add(powerControllerDevice);
    	
    	}
    	}
    	return powerDevices;
    }
}
