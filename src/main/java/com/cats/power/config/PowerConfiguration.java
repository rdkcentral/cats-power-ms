package com.cats.power.config;

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

import java.util.List;


import com.cats.power.model.PowerDevice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * The Class PowerConfiguration.
 * Description : The Class PowerConfiguration is to store the configuration of power device(s) on the rack.
 * Configuration is read from the file prod.yml located within /powerms directory on the rack.
 */
@Component
@ConfigurationProperties
@PropertySource(value="file:./powerms/prod.yml",factory =YamlPropertySourceFactory.class)
public class PowerConfiguration 
{
	
    public List<PowerDevice> powerDevices;
    
    @Value("${slotMappingFilePath}")
    public String slotMappingFilePath;
    
    @Value("${application.name}")
    private String applicationName;

    @Value("${build.version}")
    private String buildVersion;


	public String getSlotMappingFilePath()
    {
        return slotMappingFilePath;
    }

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	public List<PowerDevice> getPowerDevices() {
		return powerDevices;
	}

	public void setPowerDevices(List<PowerDevice> powerDevices) {
		this.powerDevices = powerDevices;
	}

	public void setSlotMappingFilePath(String slotMappingFilePath) {
		this.slotMappingFilePath = slotMappingFilePath;
	}

	
}
