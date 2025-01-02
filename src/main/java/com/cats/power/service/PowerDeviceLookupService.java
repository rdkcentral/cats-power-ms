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
import com.cats.power.exceptions.SlotMappingException;
import com.cats.power.model.OutletInfo;
import com.cats.power.utils.PowerOutletController;
import com.cats.power.utils.PowerOutletFactory;
import com.cats.power.utils.SlotToPortMappings;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PowerDeviceLookupService {
    private static final Logger log = LoggerFactory.getLogger(PowerDeviceLookupService.class);
    /**
     * List of power devices configured.
     */
    private SlotToPortMappings slotToPortMappings;
    @Autowired
    private PowerOutletFactory factory;
    @Autowired
    private PowerDeviceManager deviceManager;
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private PowerConfiguration pconfig;
    private String MAPPING_FILEPATH = "";

    @Autowired
    public PowerDeviceLookupService(PowerOutletFactory factory, PowerDeviceManager deviceManager, PowerConfiguration pconfig) {
        this.factory = factory;
        this.deviceManager = deviceManager;
        this.pconfig = pconfig;
        initializePortMapping();
    }

    public PowerDeviceLookupService(PowerOutletFactory factory, PowerDeviceManager deviceManager, String filePath) {
        this.factory = factory;
        this.deviceManager = deviceManager;
        this.MAPPING_FILEPATH = filePath;
        initializePortMapping();
    }

    private void initializePortMapping() {
        deviceManager.getAllPowerDevices().stream().forEach(device -> {
            device.getPowerInfo().getOutlets().forEach(outlet -> {
                outlet.setSlot(null);
            });
        });
        try {
            MAPPING_FILEPATH = pconfig.getSlotMappingFilePath();
            FileInputStream in = new FileInputStream(new File(MAPPING_FILEPATH));
            slotToPortMappings = mapper.readValue(in, SlotToPortMappings.class);
            if (slotToPortMappings.getMappings().isEmpty()) {
                setDefaultMappings();
                return;
            }
            slotToPortMappings.getMappings().entrySet().forEach(kv -> {
                if (!isValidMapping(kv.getValue())) {
                    try {
                        log.error("Incompatible slot mapping found, reverting to defaults");
                        removeMappings();
                        setDefaultMappings();
                        return;
                    } catch (IOException ex) {
                    }
                }
                String slot = kv.getKey();
                if (!kv.getValue().equals("N/A")) {
                    String[] deviceAndPort = kv.getValue().split(":");
                    String device = deviceAndPort[0];
                    int outlet = Integer.parseInt(deviceAndPort[1]);
                    log.info("Custom mapping: device: " + device + " outlet: " + outlet + " slot: " + slot);
                    deviceManager.getPowerControllerDeviceById(device).getPowerInfo().getOutlet(outlet).setSlot(slot);
                }
            });
            log.info("Setting remaining slots to default ports");
            setRemainingMappings();
        } catch (IOException ex) {
            log.error("Could not process slot mappings file, using default values: " + ex.getLocalizedMessage());
            slotToPortMappings = new SlotToPortMappings();
            setDefaultMappings();
        }
    }

    public void setDefaultMappings() {
        int slot = 1;
        for (PowerControllerDevice powerDevice : deviceManager.getAllPowerDevices()) {
            for (OutletInfo outlet : powerDevice.getPowerInfo().getOutlets()) {
                log.info("Setting default for slot " + slot);
                outlet.setSlot(String.valueOf(slot));
                slot++;
            }
        }
    }

    public SlotToPortMappings getMappings() {
        //if(null == slotToPortMappings || slotToPortMappings.getMappings().isEmpty()) {
        //	this.initializePortMapping();
        //}
        return slotToPortMappings;
    }

    public SlotToPortMappings setMappings(Map<String, String> mappings) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAPPING_FILEPATH))) {
            for (Entry<String, String> entry : mappings.entrySet()) {
                if (!isValidMapping(entry.getValue())) {
                    log.error("Invalid mapping for slot " + entry.getKey() + ": " + entry.getValue());
                    writer.write(mapper.writeValueAsString(this.slotToPortMappings));
                    throw new SlotMappingException("Invalid mapping for slot " + entry.getKey() + ": " + entry.getValue());
                }
            }
            log.info("Setting new mapping: " + mapper.writeValueAsString(mappings));

            this.slotToPortMappings.setMappings(mappings);
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            log.info("Slot to port mappings file updated");
        } catch (IOException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
        initializePortMapping();
        return this.slotToPortMappings;
    }

    public void removeMappings() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAPPING_FILEPATH))) {
            log.info("Removing slot to port mappings");
            this.slotToPortMappings.removeMappings();
            log.info("Slot to port mappings have been removed");
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            setDefaultMappings();
        } catch (IOException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }

    public String getMapping(String slot) throws SlotMappingException {
        try {
            return slotToPortMappings.getMapping(slot);
        } catch (SlotMappingException ex) {
            log.error("Could not locate mapping for slot: " + slot);
            throw ex;
        }
    }

    public SlotToPortMappings setMapping(String slot, String mapping) throws IOException, SlotMappingException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAPPING_FILEPATH))) {
            if (!isValidMapping(mapping)) {
                log.error("Invalid mapping for slot " + slot + ": " + mapping);
                writer.write(mapper.writeValueAsString(this.slotToPortMappings));
                throw new SlotMappingException("Invalid mapping for slot " + slot + ": " + mapping);
            }
            log.info("Setting mapping on slot " + slot + " to " + mapping);

            if (this.slotToPortMappings.getMappings().containsKey(slot)) {
                this.slotToPortMappings.removeMapping(slot);
            }
            this.slotToPortMappings.addMapping(slot, mapping);

            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            log.info("Slot " + slot + " mapping updated");
        } catch (IOException | SlotMappingException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
        initializePortMapping();
        return this.slotToPortMappings;
    }

    public SlotToPortMappings removeMapping(String slot) throws IOException, SlotMappingException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAPPING_FILEPATH))) {
            log.info("Removing mapping on slot " + slot);
            this.slotToPortMappings.removeMapping(slot);
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
            log.info("Slot " + slot + " mapping removed");

        } catch (IOException | SlotMappingException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
        //  initializePortMapping();
        return this.slotToPortMappings;
    }

    public PowerOutletController getOutletController(String slot) throws SlotMappingException {
        PowerOutletController outletController;
        log.info("Retrieving controller for slot: " + slot);
        if (!slotToPortMappings.getMappings().isEmpty()) {
            String deviceInfo = slotToPortMappings.getMapping(slot);

            String[] deviceAndPort = deviceInfo.split(":");
            outletController = factory.getPowerOutletController(deviceAndPort[0], Integer.parseInt(deviceAndPort[1]));
            return outletController;
        }
        return getPowerControllerWithDefaultMapping(slot);
    }

    public OutletInfo getOutletInfo(String slot) {
        for (PowerControllerDevice device : deviceManager.getAllPowerDevices()) {
            for (OutletInfo outlet : device.getPowerInfo().getOutlets()) {
                if (outlet.getSlot() != null && outlet.getSlot().equals(slot)) {
                    device.retrieveOutletStatus();
                    return outlet;
                }
            }
        }
        throw new BadRequestException(String.format("Slot %s is invalid", slot));
    }

    public List<OutletInfo> getAllOutletInfo() {
        List<OutletInfo> outlets = new ArrayList<>();
        log.info("Getting all outlet info");
        deviceManager.getAllPowerDevices().stream().forEach(device -> {
            device.retrieveOutletStatus();
            device.getPowerInfo().getOutlets().stream().forEach(outlet -> {
                if (outlet.getSlot() != null) {
                    outlets.add(outlet);
                }
            });
        });
        return outlets;
    }

    public String getSlotMapping(String slot) throws SlotMappingException {
        log.info("Getting mapping for slot: " + slot);
        if (!slotToPortMappings.getMappings().isEmpty()) {
            try {
                String deviceInfo = slotToPortMappings.getMapping(slot);
                String[] deviceAndPort = deviceInfo.split(":");
                log.info(slot + "-> " + deviceAndPort[0] + ":" + deviceAndPort[1]);
                return deviceAndPort[1];
            } catch (NullPointerException ex) {
                log.error("No mapping information for slot: " + slot);
                throw new SlotMappingException("No mapping information for slot: " + slot);
            }
        }
        return slot;
    }

    public PowerOutletController getPowerControllerWithDefaultMapping(String slot) throws SlotMappingException {
        PowerControllerDevice powerDevice;
        int numOutlets;
        int slotNumber = Integer.parseInt(slot);
        Integer index = null;
        log.info("Getting power controller with default mappings");
        for (int i = 0; i < deviceManager.getAllPowerDevices().size(); i++) {
            numOutlets = deviceManager.getAllPowerDevices().get(i).getPowerInfo().getNumOfOutlets();
            if (slotNumber <= numOutlets) {
                index = i;
                break;
            }
            slotNumber = slotNumber - numOutlets;
        }
        if (index == null) {
            throw new IllegalArgumentException("Slot " + slot + " is not a valid slot");
        }
        powerDevice = deviceManager.getAllPowerDevices().get(index);
        try {
            log.info("Device: " + powerDevice.getPowerInfo().getId() + ", port: " + slot);
            return factory.getPowerOutletController(powerDevice.getPowerInfo().getId(), slotNumber);
        } catch (SlotMappingException ex) {
            log.error("Could not get default outlet controller for slot: " + slot);
            throw ex;
        }
    }

    private boolean isValidMapping(String deviceInfo) {
        boolean valid = false;
        log.info("Validating mapping for: " + deviceInfo);
        if (deviceInfo.equals("N/A")) {
            return true;
        }
        try {
            String[] deviceAndPort = deviceInfo.split(":");
            for (PowerControllerDevice powerDevice : deviceManager.getAllPowerDevices()) {
                if (powerDevice.getPowerInfo().getId().equals(deviceAndPort[0])) {
                    if (powerDevice.getPowerInfo().getNumOfOutlets() >= Integer.parseInt(deviceAndPort[1])) {
                        valid = true;
                        break;
                    }
                }
            }
        } catch (NumberFormatException ex) {
            valid = false;
        }
        return valid;
    }

    //Used to deal with tests differing user directory
    public void setMappingFilePath(String filePath) {
        this.MAPPING_FILEPATH = filePath;
        initializePortMapping();
    }

    private void setRemainingMappings() throws IOException {

        int slot = 1;
        for (PowerControllerDevice powerDevice : deviceManager.getAllPowerDevices()) {
            for (OutletInfo outlet : powerDevice.getPowerInfo().getOutlets()) {
                String val = powerDevice.getPowerInfo().getId() + ":" + outlet.getOutlet();
                if (slotToPortMappings.getMappings().get(Integer.toString(slot)) == null) {
                    if (!slotToPortMappings.getMappings().containsValue(val)) {
                        log.info("Setting default for slot " + slot + " val " + val);
                        outlet.setSlot(String.valueOf(slot));
                        slotToPortMappings.getMappings().put(Integer.toString(slot), val);
                    } else {
                        slotToPortMappings.getMappings().put(Integer.toString(slot), "N/A");
                    }
                }
                slot++;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MAPPING_FILEPATH))) {
            writer.write(mapper.writeValueAsString(this.slotToPortMappings));
        } catch (IOException ex) {
            log.error("Could not update slot mappings: " + ex.getLocalizedMessage());
            throw ex;
        }
    }
}
