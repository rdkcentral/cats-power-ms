package com.cats.power.resources;

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
import com.cats.power.model.DeviceRequestBody;
import com.cats.power.model.OutletInfo;
import com.cats.power.model.PowerInfo;
import com.cats.power.model.View;
import com.cats.power.config.PowerConfiguration;
import com.cats.power.utils.ScheduledJobs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;

import com.cats.power.service.PowerDeviceManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Power Device Resource -> supports operations for getting and setting power devices on the rack.
 */
@RestController
@Tag(name = "Power Device Control", description = "Control APIs for Power Devices on Rack.")
@RequestMapping("/rest/device")
public class PowerDeviceResource
{
    /**
     * List of power devices configured.
     */
	@Autowired
    private  PowerDeviceManager deviceManager;
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private ScheduledJobs scheduledJobs;
    @Autowired
    public PowerConfiguration pconfig;



    /**
     * Get device details for a Power device on the rack.
     *
     * @return - Returns array response of PowerInfo for all power devices configured on the rack.
     */
    @Operation(summary = "Get Power Devices", description = "Get info on all power devices for a rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PowerInfo.class))) }),
            @ApiResponse(responseCode = "404", description = "Power devices not found")
    })
    @RequestMapping(value="/",method=RequestMethod.GET,produces="application/json")
    public String getPowerDevices()
    {
        String ret;
        try {
            ret = mapper.writerWithView(View.Deep.class).writeValueAsString(deviceManager.getAllPowerDevicesInfo());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(PowerDeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = "Could not get power devices";
        }
        return ret;
    }

    /**
     * Set device details for a Power device on the rack.
     *
     * @return {@link Response} - Returns Response as 200 Success unless returnState is set as true.
     * In this case, array response of PowerInfo is returned for configured devices.
     */
    @Operation(summary = "Set Power Device(s) for Rack", description = "Set details for Power Devices connected to the Rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PowerInfo.class))) }),
            @ApiResponse(responseCode = "400", description = "Request Body is invalid. Please update and retry request.")
    })
    @RequestMapping(value="/",method=RequestMethod.POST,produces="application/json")
    public Response setPowerDevice(@Parameter(description = "Request Body for Power Device(s) to set for Rack") @RequestBody DeviceRequestBody requestBody,
                                   @Parameter(description = "Flag for returning existing Power Info in response as entity field.")
                                   @DefaultValue("false") @QueryParam("returnState") boolean returnState) {
        Response res;
        if(!isValidRequestBody(requestBody)){
            res = Response.status(Response.Status.BAD_REQUEST)
                    .entity("The request body was invalid, check device ID, outlet number, and outlet state").build();
            return res;
        }
        
        requestBody.getDevices().stream().forEach(device -> setPowerDevice(device.getId(), device, false));
        
        if(returnState){
            String entity = getPowerDevices();
            res = Response.status(Response.Status.OK).entity(entity).build();
        }
        else {
            res = Response.status(Response.Status.NO_CONTENT).build();
        }
        return res;
    }

    /**
     * Validate the request body for Power Device.
     *
     * @return - Returns true if request body is valid, false otherwise.
     */
    public boolean isValidRequestBody(DeviceRequestBody requestBody) {
        boolean isValid = true;
        try{
            for(PowerInfo requestDevice : requestBody.getDevices()){
                if(! isValidRequestBody(requestDevice)){
                    isValid = false;
                    return isValid;
                }
            }
        }
        catch(Exception ex){
            isValid = false;
            return isValid;
        }
        return isValid;
    }
    
     /**
     * Get version of power service.
     * 
     * @return - version of power service.
     */
     @Operation(summary = "Get Service Version", description = "Get version of Power Service deployed on Rack.")
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "operation successful",
                     content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) })
     })
     @RequestMapping(value="/version",method=RequestMethod.GET ,produces= "text/plain")
    public String getVersion()
    {
        String version = "Unknown";

        if (null != pconfig.getBuildVersion())
        {
            version = pconfig.getBuildVersion();
        }
        return version;
    }


    /**
     * Get device details for a Power device on the rack for a given device ID.
     *
     * @return {@link PowerInfo} - Returns response of PowerInfo for given device ID.
     */
    @Operation(summary = "Get Power Device", description = "Get info on a power device given the device ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PowerInfo.class)) }),
            @ApiResponse(responseCode = "404", description = "Power device not found."),
            @ApiResponse(responseCode = "503", description = "Cannot connect to provided power device ID.")
    })
    @RequestMapping(value = "/{deviceId}",method=RequestMethod.GET,produces= "text/plain")
    public String getPowerDevice(@Parameter(description = "Device ID to get details of.") @PathVariable("deviceId") String deviceId)
    {
        String ret;
        try {
            ret = mapper.writerWithView(View.Deep.class).writeValueAsString(deviceManager.getPowerDeviceInfoById(deviceId));
        } catch (JsonProcessingException ex) {
            Logger.getLogger(PowerDeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = "Could not get power device";
        }
        return ret;
    }


    /**
     * Set device details for a Power device on the rack for a given device ID.
     *
     * @return {@link Response} - Returns Response as 200 Success unless returnState is set as true.
     * In this case, response of PowerInfo is returned.
     */
    @Operation(summary = "Set Power Device", description = "Set info on a power device given the device ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PowerInfo.class)) }),
            @ApiResponse(responseCode = "400", description = "Request Body is invalid. Please update and retry request.")
    })
    @RequestMapping(value = "/{deviceId}",method=RequestMethod.POST,produces={"application/json"})
    public Response setPowerDevice(@Parameter(description = "Device ID to set/update details of.") @PathVariable("deviceId") String deviceId,
                                   @Parameter(description = "Request Body for Power Device to set for given device ID.") @RequestBody PowerInfo requestBody,
                                   @Parameter(description = "Flag for returning existing Power Info in response as entity field.")
                                   @DefaultValue("false") @QueryParam("returnState") boolean returnState) {
        PowerControllerDevice device;
        requestBody.setId(deviceId);
        Response res;
        if(!isValidRequestBody(requestBody)){
            res = Response.status(Response.Status.BAD_REQUEST)
                    .entity("The request body was invalid, check device ID, outlet number, and outlet state").build();
            return res;
        }
        
        device = deviceManager.getPowerControllerDeviceById(deviceId);
        requestBody.getOutlets().stream().forEach(outlet -> {
            String status = outlet.getStatus();
            switch (status) {
                case "OFF":
                    device.powerOff(outlet.getOutlet());
                    break;
                case "ON":
                    device.powerOn(outlet.getOutlet());
                    break;
                case "REBOOT":
                    device.powerToggle(outlet.getOutlet());
                    break;
                default:
                    break;
            }
        });
        
        if(returnState){
            String entity = getPowerDevice(deviceId);
            res = Response.status(Response.Status.OK).entity(entity).build();
        }
        else{
            res = Response.status(Response.Status.NO_CONTENT).build();
        }
        return res;
    };

    /**
     * Validate the request body for Power Device.
     *
     * @return - Returns true if request body is valid, false otherwise.
     */
    private boolean isValidRequestBody(PowerInfo requestBody){
        boolean isValid = true;
        String status;
        PowerControllerDevice device;
        try{
            device = deviceManager.getPowerControllerDeviceById(requestBody.getId());
            for(OutletInfo outlet : requestBody.getOutlets()){
                if(0 >= outlet.getOutlet() || outlet.getOutlet() > device.getPowerInfo().getNumOfOutlets()){
                    isValid = false;
                    return isValid;
                }
                status = outlet.getStatus().toUpperCase();
                if( !(status.equals("OFF") || status.equals("ON") || status.equals("REBOOT") || status.equals("IGNORE"))){
                    isValid = false;
                    return isValid;
                }
            }
        }
        catch(Exception ex){
            isValid = false;
            return isValid;
        }
        return isValid;
    }


    /**
     * Perform power on operation on a specific outlet for a given Power device.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power ON a Power Device Outlet", description = "Set a given power device outlet to ON.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device/outlet IDs and retry request.")
    })
    @RequestMapping(value = "/{deviceId}/{outlet}/on",method=RequestMethod.POST)
    public Boolean on(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId,
                      @Parameter(description = "Outlet ID of given Power Device to power ON.") @PathVariable("outlet") Integer outlet)
    {
        synchronized(String.valueOf(outlet).intern()){
            validateOutlet(deviceId, outlet);
            PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
            return device.powerOn(outlet);
        }
    }


    /**
     * Perform power off operation on a specific outlet for a given Power device.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power OFF a Power Device Outlet", description = "Set a given power device outlet to OFF.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device/outlet IDs and retry request.")
    })
    @RequestMapping(value = "/{deviceId}/{outlet}/off",method=RequestMethod.POST)
    public Boolean off(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId,
                       @Parameter(description = "Outlet ID of given Power Device to power OFF.") @PathVariable("outlet") Integer outlet)
    {
        synchronized(String.valueOf(outlet).intern()){
            validateOutlet(deviceId, outlet);
            PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
            return device.powerOff(outlet);
        }
    }


    /**
     * Perform power reboot operation on a specific outlet for a given Power device.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "REBOOT a Power Device Outlet", description = "REBOOT a given power device outlet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device/outlet IDs and retry request.")
    })
    @RequestMapping(value = "/{deviceId}/{outlet}/reboot",method=RequestMethod.POST)
    public Boolean reboot(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId,
                          @Parameter(description = "Outlet ID of given Power Device to REBOOT.") @PathVariable("outlet") Integer outlet)
    {
        synchronized(String.valueOf(outlet).intern()){
            validateOutlet(deviceId, outlet);
            PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
            return device.powerToggle(outlet);
        }
    }


    /**
     * Get the power status for a specific outlet on a given Power device.
     *
     * @return {@link String} - Detailed view of all Power devices on the rack.
     */
    @Operation(summary = "Get Power Status", description = "Get Current Power Status for a given Power Device Outlet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device/outlet IDs and retry request.")
    })
    @RequestMapping(value = "/{deviceId}/{outlet}/status",method=RequestMethod.GET,produces="text/plain")
    public String status(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId,
                         @Parameter(description = "Outlet ID of given Power Device to get current Power Status of.") @PathVariable("outlet") Integer outlet)
    {
        validateOutlet(deviceId, outlet);
        String ret;
            PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
            ret = device.getOutletStatus(outlet);
        return ret;
    }


    /**
     * Perform power reboot operation on all outlets for every power device on the rack.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power OFF All Device Outlets", description = "Power OFF all Power Device Outlets configured for the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power OFF all device outlets. Please try again.")
    })
    @RequestMapping(value = "/all/off",method=RequestMethod.POST)
    public Boolean allOff()
    {
    	Boolean retVal = true;
    	for(PowerInfo powerInfo : deviceManager.getAllPowerDevicesInfo()){
            try{
    		retVal = allOffOnDevice(powerInfo.getId());
            }
            catch(Exception ex){
                retVal = false;
            }
    	}
    	
        return retVal;
    }


    /**
     * Perform power on operation on all outlets for every power device on the rack.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power ON All Device Outlets", description = "Power ON all Power Device Outlets configured for the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power ON all device outlets. Please try again.")
    })
    @RequestMapping(value = "/all/on",method=RequestMethod.POST)
    public Boolean allOn()
    {
    	Boolean retVal = true;
    	for(PowerInfo powerInfo : deviceManager.getAllPowerDevicesInfo()){
            try{
    		retVal = allOnDevice(powerInfo.getId());
            }
            catch(Exception ex){
                retVal = false;
            }
    	}
    	
        return retVal;
    }

    /**
     * Perform power reboot operation on all outlets for every power device on the rack.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power REBOOT All Device Outlets", description = "REBOOT all Power Device Outlets configured for the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power REBOOT for all device outlets. Please try again.")
    })
    @RequestMapping(value = "/all/reboot",method=RequestMethod.POST)
    public Boolean allReboot()
    {
    	Boolean retVal = null;
    	for(PowerInfo powerInfo : deviceManager.getAllPowerDevicesInfo()){
            retVal = allRebootOnDevice(powerInfo.getId());
    	}
    	
        return retVal;
    }

    /**
     * Get the detailed status for power service and all Power devices on the rack.
     *
     * @return {@link String} - Detailed view of overall Power status.
     */
    @Operation(summary = "Get All Power Statuses", description = "Get Current Power Status for all Power Devices.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)) })
    })
    @RequestMapping(value = "/all/status",method=RequestMethod.GET,produces ={"application/json"})
    public String allStatus()
    {   
        String powerInfo;
        String ret;
        
        try {
            powerInfo = mapper.writerWithView(View.Deep.class).writeValueAsString(deviceManager.getAllPowerDevicesInfo());
            ret = String.format("{\"version\": \"%s\", \"devices\": %s}", pconfig.getBuildVersion(), powerInfo);
        } catch (IOException ex) {
            Logger.getLogger(PowerDeviceResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = "Could not get power status";
        }
        return ret;
    }


    /**
     * Perform power off operation for all outlets on a given power device ID.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power OFF Outlets For Power Device", description = "Power OFF all outlets for a given power device.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power OFF all outlets for given power device. Please try again.")
    })
    @RequestMapping(value = "/{deviceId}/all/off",method=RequestMethod.POST)
    public Boolean allOffOnDevice(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId)
    {
    	 PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
         return device.powerOffAllOutlets();
    }


    /**
     * Perform power on operation for all outlets on a given power device ID.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power ON Outlets For Power Device", description = "Power ON all outlets for a given power device.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power ON all outlets for given power device. Please try again.")
    })
    @RequestMapping(value = "/{deviceId}/all/on",method=RequestMethod.POST)
    public Boolean allOnDevice(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId)
    {
    	PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
        return device.powerOnAllOutlets();
    }

    /**
     * Perform power reboot operation for all outlets on a given power device ID.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power REBOOT Outlets For PowerDevice", description = "Power REBOOT all outlets for a given power device.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power REBOOT all outlets for given power device. Please try again.")
    })
    @RequestMapping(value = "/{deviceId}/all/reboot",method=RequestMethod.POST)
    public Boolean allRebootOnDevice(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId)
    {
    	PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
        return device.rebootAllOutlets();
    }


    /**
     * Get the power status for all Power device outlets on the rack.
     *
     * @return {@link PowerInfo} - Detailed view of all Power devices on the rack.
     */
    @Operation(summary = "Get All Power Statuses for Device Outlets", description = "Get Current Power Status for all outlets given Power Device ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PowerInfo.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device ID and retry request.")
    })
    @RequestMapping(value = "/{deviceId}/all/status",method=RequestMethod.GET,produces = {"application/json"})
    public PowerInfo allStatusOfDevice(@Parameter(description = "ID of Power Device on Rack.") @PathVariable("deviceId") String deviceId)
    {
    	PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
    	return device.getPowerInfo();
    }

    /**
     * Validates if device outlet exists based on max ports for Power device.
     */
    private void validateOutlet(String deviceId, Integer outlet)
    {
            PowerInfo powerInfo = deviceManager.getPowerControllerDeviceById(deviceId).getPowerInfo();
            if (outlet < 1 || outlet > powerInfo.getNumOfOutlets())
            {
                throw new IllegalArgumentException("Outlet " + outlet + " is invalid for device " + deviceId
                        + ". Max Port available is " + powerInfo.getNumOfOutlets());
            }
    }


    /**
     * Get metadata and version data for given Power Device ID.
     *
     * @return {@link Map<>} - returns nested Map<> containing metadata and version information of Power Device.
     */
    @Operation(summary = "Get Metadata For Power Device", description = "Get metadata and version data for given Power Device ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Invalid Request. Please check device ID and retry request.")
    })
    @RequestMapping(value = "/{deviceId}/metaInfo",method=RequestMethod.GET,produces = {"application/json"})
    public Map<String,Map<String,String>> getVersionAndMetadata(@Parameter(description = "ID of Power Device on Rack.")
                                                                @PathVariable("deviceId") String deviceId) {
        Map<String,Map<String,String>> infoMap = new HashMap<>();
        PowerControllerDevice device = deviceManager.getPowerControllerDeviceById(deviceId);
        if(device != null) {
        	ScheduledJobs.setPowerDevices(pconfig.getPowerDevices());
            infoMap.put("version", scheduledJobs.getVersion().get(deviceId));
            infoMap.put("metadata", scheduledJobs.getMetadata().get(deviceId));
            return infoMap;
        }
        else {
            throw new NotFoundException("Unable to find device with id " + deviceId);
        }

    }
}
