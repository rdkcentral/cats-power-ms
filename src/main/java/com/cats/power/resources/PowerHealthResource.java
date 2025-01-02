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

import com.cats.power.model.HealthReport;
import com.cats.power.model.HealthStatusBean;
import com.cats.power.config.PowerConfiguration;
import com.cats.power.model.PowerInfo;
import com.cats.power.service.DefaultPowerDeviceManager;
import com.cats.power.utils.PowerDeviceHealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * PowerHealthResource supports operations for getting health of Power service,
 * hardware, and its dependencies on the rack.
 * */
@Tag(name = "Power Health", description = "Health APIs for Power Service")
@RestController
@RequestMapping("/rest")
public class PowerHealthResource {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private PowerConfiguration pconfig;
    Logger logger = LoggerFactory.getLogger(PowerHealthResource.class);

    @Autowired
    DefaultPowerDeviceManager powerDeviceManager;


    /**
     * Get health of Power service, hardware, and its dependencies on the rack.
     *
    * @return - health of the Power capability as HealthStatusBean.
     *
     * */
    @Operation(summary = "Get Health of Power on the Rack", description = "Get health info on Power HW and dependencies for the rack.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "operation successful",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PowerInfo.class)) }),
            @ApiResponse(responseCode = "404", description = "Power device not found."),
            @ApiResponse(responseCode = "503", description = "Cannot connect to provided power device ID.")
    })
    @RequestMapping(value = "/health",method=RequestMethod.GET ,produces= "application/json")
    public HealthStatusBean getPowerHealth() {
        HealthStatusBean result = new HealthStatusBean();
        PowerDeviceHealthCheck powerhealthCheck =new PowerDeviceHealthCheck(powerDeviceManager);

        try {
            List<HealthReport> reports = Arrays.asList(mapper.readValue(powerhealthCheck.check(), HealthReport[].class));
            result.setHwDevicesHealthStatus(reports);
            result.setIsHealthy(true);            
            if(result.getVersion() == null){
                result.setVersion(new HashMap<>());
            }
            result.getVersion().put("MS_VERSION",getMicroServiceVersion());
        }
        catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        return result;
    }

    /**
     * Get version of Power microservice.
     *
     * @return - version of the Power microservice deployment on the rack.
     * */
    public String getMicroServiceVersion() throws IOException {
        String version = pconfig.getBuildVersion();
		if(version == null || version.isEmpty()) {
		    version = "development";
		}
		return version;
    }

}
