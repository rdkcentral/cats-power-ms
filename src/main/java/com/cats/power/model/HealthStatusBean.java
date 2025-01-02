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
import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * HealthStatusBean: Expected health object returned for health reporting.
 */
@Data
@Schema(name = "HealthStatusBean", description = "Health Status details for Power Service")
public class HealthStatusBean {

    /**
     * @return the version details of the Power service
     */
    Map<String,String> version = new HashMap<>();

    /**
     * @return the isHealthy status for Power Service
     */
    Boolean isHealthy;

    /**
     * @return the remarks for Power Service Health
     */
    List<HealthReport> hwDevicesHealthStatus;

    /**
     * @return the dependencies health status for Power Service
     */
    List<HealthReport> dependenciesHealthStatus;
}