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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * PowerInfo: Used to store power device information.
 */
@XmlRootElement
@JsonIgnoreProperties(value = {"scheme", "state"}, ignoreUnknown = true)
@Schema(name = "PowerInfo", description = "Power Information for a given Power Device")
public class PowerInfo implements Serializable {

	private static final long serialVersionUID = 6164030050030751071L;

    /**
     * @return type of the power device
     */
    @JsonView(View.Shallow.class)
	private String type;

    /**
     * @return scheme of the power device
     */
	private String scheme;

    /**
     * @return IP address of the power device
     */
    @JsonView(View.Deep.class)
	private String ip;

    /**
     * @return port of the power device
     */
    @JsonView(View.Deep.class)
	private int port;

    /**
     * @return number of outlets in the power device
     */
    @JsonView(View.Deep.class)
	private int numOfOutlets;

    /**
     * @return id of the power device
     */
    @JsonView(View.Shallow.class)
    private String id;

    /**
     * @return state of the power device
     */
	private String state;

    /**
     * @return list of outlets in the power device
     */
    @JsonView(View.Shallow.class)
    private List<OutletInfo> outlets        = new ArrayList<>();
			
	public PowerInfo(){
		
	}
	
	public PowerInfo(String type, String ip, int port) {
		this.type = type;
		this.ip = ip;
		this.port = port;
	}
	
	@XmlAttribute(name="outlets")
	public int getNumOfOutlets( ) {
		return numOfOutlets;
	}

	public void setNumOfOutlets(int numOfOutlets) {
		this.numOfOutlets = numOfOutlets;
	}

	@XmlAttribute(name="type")
	public String getType() {
		return type;
	} 
	
	public void setType(String type) {
		this.type = type;
	} 
	
	@XmlAttribute(name="IPaddress")
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	@XmlAttribute(name="port")
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
        
        public List<OutletInfo> getOutlets(){
            return outlets;
        }
        
        public void setOutlets(List<OutletInfo> outlets){
            this.outlets = outlets;
        }
        
        public OutletInfo getOutlet(int outlet){
            OutletInfo outletInfo;
            try{
                outletInfo = this.getOutlets().stream()
                    .filter(state -> state.getOutlet() == outlet)
                    .findFirst()
                    .get();
            }
            catch(NoSuchElementException ex){
                outletInfo = new OutletInfo();
                outletInfo.setOutlet(outlet);
                outletInfo.setStatus("UNKNOWN");
                outlets.add(outletInfo);
            }
            return outletInfo;
        }
        
        public String getOutletStatus(int outlet){
            OutletInfo outletInfo;
            outletInfo = getOutlet(outlet);
            return outletInfo.getStatus();
        }
        
        public void setOutletStatus(int outlet, String status){
            OutletInfo outletInfo;
            outletInfo = getOutlet(outlet);
            outletInfo.setStatus(status);
        }
        
	@Override
	public String toString(){
            StringBuilder ret = new StringBuilder();
            ret.append("IP: ").append(ip)
                .append("Port: ").append(port)
                .append("Type: ").append(type)
                .append("ID: ").append(id)
                .append("State: ").append(state)
                .append("Scheme: ").append(scheme)
                .append("Num Of Outlets: ").append(numOfOutlets)
                .append("Outlet State: [");
            
            for(int i = 0; i < numOfOutlets - 1; i++){
                ret.append("{").append(getOutlets().get(i).toString()).append("}, ");
            }
            
            ret.append("{").append(getOutlets().get((numOfOutlets - 1)).toString()).append("}]");
            return ret.toString();
	}

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @param scheme the scheme to set
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }
}
