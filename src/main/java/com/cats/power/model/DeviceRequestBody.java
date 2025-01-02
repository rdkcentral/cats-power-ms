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

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;


/**
 * DeviceRequestBody: Defines expected Request body for a given Power Device
 */
@Schema(name = "DeviceRequestBody", description = "Request body for a given Power Device")
public class DeviceRequestBody {
    private List<PowerInfo> devices = new ArrayList<>();

    /**
     * @return the devices
     */
    public List<PowerInfo> getDevices() {
        return devices;
    }

    /**
     * @param devices - the power devices to set
     */
    public void setDevices(List<PowerInfo> devices) {
        this.devices = devices;
    }
}
