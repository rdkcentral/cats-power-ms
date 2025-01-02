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

import com.cats.power.exceptions.SlotMappingException;
import com.cats.power.model.OutletInfo;
import com.cats.power.model.PowerInfo;
import com.cats.power.model.View;
import com.cats.power.config.PowerConfiguration;
import com.cats.power.service.PowerDeviceLookupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Power Slot Rest Resource -> Supporting operations for power at the slot level.
 */
@RestController
@Tag(name = "Power Slot Level Control", description = "Control APIs for Power Enabled Slots on a Rack.")
@RequestMapping("/rest/slot")
public class PowerSlotResource
{
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private PowerDeviceLookupService powerDeviceLookupService;
    @Autowired
    private PowerConfiguration pconfig;


    /**
     * Get status of Power Outlet given slot details.
     *
     * @return {@link String} - status of Power for the provided slot.
     *
     * */
    @Operation(summary = "Get Status of Power Device Outlet for a Slot", description = "Get details on Power Version and Outlet Status for a given slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
    })
    @RequestMapping(value="/{slot}",method=RequestMethod.GET ,produces= "text/plain")
    public String get(@Parameter(description = "Slot to get details for.") @PathVariable("slot") String slot) throws SlotMappingException
    {
        String status = powerDeviceLookupService.getOutletController(slot).getOutletStatus();
        return "POWER Version " + getVersion(slot) + " [ Slot=" + slot + ", Status=" + status + "]";
    }

    /**
     * Perform power off operation.
     * The return type is changed to String (text/plain) from Boolean (application/json) as a backward compatibility
     * for the existing cats client version's since it is expecting the response to be in text/plain even though the
     * value is an actual Boolean type.
     * 
     * @return {@link String} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power OFF Slot", description = "Power OFF Power Outlet configured for the slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "True if power operation is successful, false otherwise",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power OFF slot. Please try again.")
    })
    @RequestMapping(value="/{slot}/off",method=RequestMethod.POST,produces = "text/plain")
    public String off(@Parameter(description = "Slot to power OFF.") @PathVariable("slot") String slot) throws SlotMappingException
    {
        synchronized(String.valueOf(slot).intern()){
            return String.valueOf(powerDeviceLookupService.getOutletController(slot).powerOff());
        }
    }

    /**
     * Perform power on operation.
     * The return type is changed to String (text/plain) from Boolean (application/json) as a backward compatibility
     * for the existing cats client version's since it is expecting the response to be in text/plain even though the
     * value is an actual Boolean type.
     * 
     * @return {@link String} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power ON Slot", description = "Power ON Power Outlet configured for the slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "True if power operation is successful, false otherwise",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power ON slot. Please try again.")
    })
    @RequestMapping(value="/{slot}/on",method=RequestMethod.POST,produces = "text/plain")
    public String on(@Parameter(description = "Slot to power ON.") @PathVariable("slot") String slot) throws SlotMappingException
    {
        synchronized(String.valueOf(slot).intern()){
            return String.valueOf(powerDeviceLookupService.getOutletController(slot).powerOn());
        }
    }

    /**
     * Perform power reboot operation.
     * The return type is changed to String (text/plain) from Boolean (application/json) as a backward compatibility
     * for the existing cats client version's since it is expecting the response to be in text/plain even though the
     * value is an actual Boolean type. The newer set of API's are moved under v2 version which returns an actual
     * Boolean value which produces in the format application/json.
     *
     * @return {@link String} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power REBOOT Slot", description = "Power REBOOT Power Outlet configured for the slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "True if power operation is successful, false otherwise",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power REBOOT slot. Please try again.")
    })
    @RequestMapping(value="/{slot}/reboot",method=RequestMethod.POST,produces="text/plain")
    public String reboot(@Parameter(description = "Slot to power REBOOT.") @PathVariable("slot") String slot) throws SlotMappingException
    {
        synchronized(String.valueOf(slot).intern()){
            return String.valueOf(powerDeviceLookupService.getOutletController(slot).powerToggle());
        }
    }

    /**
     * Return the power status of a power device.
     * 
     * @return status of a power device.
     */
    @Operation(summary = "Get Status of Power Outlet for a Slot", description = "Get status details on Power Outlet for a given slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot get status of power outlet for slot. Please try again.")
    })
    @RequestMapping(value="/{slot}/status",method=RequestMethod.GET ,produces= "text/plain")
    public String status(@Parameter(description = "Slot to get Power status of.") @PathVariable("slot") String slot) throws SlotMappingException
    {
        return powerDeviceLookupService.getOutletInfo(slot).getStatus();
    }

    /**
     * Get version of power service.
     * 
     * @return - version of power service.
     */
    @Operation(summary = "Get Build Version For Slot", description = "Get build version details for a given slot on the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot get build version details for slot. Please try again.")
    })
    @RequestMapping(value="/{slot}/version",method=RequestMethod.GET ,produces= "text/plain")
    public String getVersion(@Parameter(description = "Slot to get Power version of.") @PathVariable("slot") String slot) throws SlotMappingException
    {
        String version = "Unknown";

            version = pconfig.getBuildVersion();
            
        return version;
    }
    
    /**
     * Return the power status of all slots.
     * 
     * @return status of a power device.
     */
    public String getAllSlotStatus(){
        String outletInfo;
        String ret;
        try {
            outletInfo = mapper.writerWithView(View.Slot.class).writeValueAsString(powerDeviceLookupService.getAllOutletInfo());
            ret = String.format("{\"outlets\": %s}", outletInfo);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(PowerSlotResource.class.getName()).log(Level.SEVERE, null, ex);
            ret = "Could not get slot status";
        }
        return ret;
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
    @RequestMapping(value="/all/on",method=RequestMethod.POST)
    public boolean allOn(){
        boolean ret = true;
        for(OutletInfo outlet: powerDeviceLookupService.getAllOutletInfo()){
            powerDeviceLookupService.getOutletController(outlet.getSlot()).powerOn();
        }
        return ret;
    }

    /**
     * Perform power off operation on all outlets for every power device on the rack.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power OFF All Device Outlets", description = "Power OFF all Power Device Outlets configured for the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power OFF all device outlets. Please try again.")
    })
    @RequestMapping(value="/all/off",method=RequestMethod.POST)
    public boolean allOff(){
        boolean ret = true;
        for(OutletInfo outlet: powerDeviceLookupService.getAllOutletInfo()){
            powerDeviceLookupService.getOutletController(outlet.getSlot()).powerOff();
        }
        return ret;
    }

    /**
     * Perform power reboot operation on all outlets for every power device on the rack.
     *
     * @return {@link Boolean} - True if power operation is successful, false otherwise.
     */
    @Operation(summary = "Power REBOOT All Device Outlets", description = "Power REBOOT all Power Device Outlets configured for the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)) }),
            @ApiResponse(responseCode = "400", description = "Cannot process request to power REBOOT all device outlets. Please try again.")
    })
    @RequestMapping(value="/all/reboot",method=RequestMethod.POST)
    public boolean allReboot(){
        boolean ret = true;
        for(OutletInfo outlet: powerDeviceLookupService.getAllOutletInfo()){
            powerDeviceLookupService.getOutletController(outlet.getSlot()).powerToggle();
        }
        return ret;
    }


    /**
     * Set custom power details for all power devices on the rack.
     *
     * @return {@link Response} - Returns Response as 200 Success unless returnState is set as true.
     * In this case, array response of PowerInfo is returned for configured devices.
     */
    @Operation(summary = "Set Custom Power Device Details", description = "Set custom details for Power Devices connected to the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PowerInfo.class))) }),
            @ApiResponse(responseCode = "400", description = "Request Body is invalid. Please update and retry request.")
    })
    @RequestMapping(value="/all",method=RequestMethod.POST)
    public Response customPower(@Parameter(description = "If true, return state after setting Slot Mappings.")
                                @DefaultValue("false") @QueryParam("returnState") boolean returnState,
                                @Parameter(description = "Request Body for Custom Power Details to set for all devices.")
                                @RequestBody PowerInfo requestBody) throws BadRequestException
    {
        Response res;
        String status;
        if(!isValidCustomPower(requestBody)){
           throw new BadRequestException("Bad slot power request, check slots and power states in request");
        }
        for(OutletInfo outlet: requestBody.getOutlets()){
            status = outlet.getStatus().toUpperCase();
            switch (status) {
                case "OFF":
                    powerDeviceLookupService.getOutletController(outlet.getSlot()).powerOff();
                    break;
                case "ON":
                    powerDeviceLookupService.getOutletController(outlet.getSlot()).powerOn();
                    break;
                case "REBOOT":
                    powerDeviceLookupService.getOutletController(outlet.getSlot()).powerToggle();
                    break;
                default:
                    break;
            }
        }
        if(returnState){
            String entity = getAllSlotStatus();
            res = Response.status(Response.Status.OK).entity(entity).build();
        }
        else{
            res = Response.status(Response.Status.NO_CONTENT).build();
        }
        return res;
    }


    /**
     * Validate the custom power request.
     *
     * @param requestBody - PowerInfo object containing custom power details.
     * @return - true if the request is valid, false otherwise.
     */
    private boolean isValidCustomPower(PowerInfo requestBody){
        boolean isValid = true;
        String status;
        for(OutletInfo outlet : requestBody.getOutlets()){
            try{
                powerDeviceLookupService.getOutletController(outlet.getSlot());
            }
            catch(SlotMappingException ex){
                isValid = false;
                return isValid;
            }
            try{
                status = outlet.getStatus();
                if( !( status.equals("ON") || status.equals("OFF") || 
                        status.equals("REBOOT") || status.equals("IGNORE") ) ){
                    isValid = false; 
                    return isValid;
                }
            }
            catch(NullPointerException ex){
                isValid = false;
                return isValid;
            }
        }
        return isValid;
    }
}
