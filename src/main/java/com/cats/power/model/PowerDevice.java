package com.cats.power.model;

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

import com.cats.power.service.PowerDeviceType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * PowerDevice: Defines Power Device given common information
 */
@Schema(name = "PowerDevice", description = "Power Device Information")
public class PowerDevice {

    /**
     * @return the host address of the power device
     */
    private String host;

    /**
     * @return the port of the power device
     */
    private Integer port;

    /**
     * @return the maxPorts of the device
     */
    private Integer maxPorts;

    /**
     * @return the deviceId of the power device
     */
    private String deviceId;

    /**
     * @return the type of power device
     */
    private String type;

    /**
     * @return the userName of the device for login
     */
    private String userName;

    /**
     * @return the password of the device for login
     */
    private String password;

    public Integer getPort()
    {
        return port;
    }

    public void setPort( Integer port )
    {
        this.port = port;
    }

    public void setHost( String host )
    {
        this.host = host;
    }
   
    public String getHost() {
        return host;
    }
    
    public Integer getMaxPort()
    {
        return maxPorts;
    }

    public void setMaxPort( Integer maxPorts )
    {
        this.maxPorts = maxPorts;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId( String deviceId )
    {
        this.deviceId = deviceId;
    }

	public String getType() {
		return type;
	}

    /**
     * Set the type of the power device.
     * Validate if set type is supported.
     * @param type
     */
	public void setType(String type) {
	    System.out.println("Setting for: " + type);
            System.out.println(PowerDeviceType.values().length);
	    if(PowerDeviceType.findType(type) == null){
	        
	        String supportedTypes = "";
	        for(PowerDeviceType powerDeviceType : PowerDeviceType.values()){
	            supportedTypes += powerDeviceType.getScheme()+",";
	        }
	        
	        throw new IllegalArgumentException("Type "+type+" is not supported. Supported types are "+supportedTypes);
	    }
	    
		this.type = type;
	}
        
        public void setUserName(String userName){
            this.userName = userName;
        }
        
        public String getUserName(){
            return userName;
        }
        
        public void setPassword(String password){
            this.password = password;
        }
        
        public String getPassword(){
            return password;
        }

}
