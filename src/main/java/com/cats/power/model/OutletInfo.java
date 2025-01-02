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

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;

/**
 * OutletInfo: Defines common information for a power device outlet.
 */
@Schema(name = "OutletInfo", description = "Outlet Information for a given Power Device")
public class OutletInfo {

    /**
     * List of possible status for a slot
     */
    private final List<String> SLOT_STATUS = Arrays.asList("ON", "OFF", "UNKNOWN", "REBOOT", "IGNORE");

    /**
     * @return the outlet number
     */
    @JsonView(View.Device.class)
    private int outlet;

    /**
     * @return the slot number
     */
    @JsonView(View.Slot.class)
    private String slot;

    /**
     * @return the status of the slot
     */
    private String status;


    public int getOutlet() {
        return outlet;
    }

    public void setOutlet(int outlet) {
        this.outlet = outlet;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if(SLOT_STATUS.contains(status.toUpperCase())){
            this.status = status.toUpperCase();
        }
    }
    
    @Override
    public String toString(){
        return String.format("Outlet: %s, Status: %s", outlet, status);
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }
}