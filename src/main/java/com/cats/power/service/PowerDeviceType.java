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

/**
 * Supported Power Device Types
 * */
public enum PowerDeviceType
{
    SYNACCESS("synaccess"),
    EATON_G3("eatonG3"),
    DIGITAL_LOGGER("digitalLogger"),
    DIGITAL_LOGGER_REST("digitalLoggerRest"),
    RARITAN("raritanPX35145R"),
    LINDY("lindy");
    
    String scheme;

    private PowerDeviceType(String scheme)
    {
        this.scheme = scheme;
    }

    /**
     * Get the scheme for a power device type.
     * @return The scheme.
     * */
    public String getScheme()
    {
        return scheme;
    }

    /**
     * Find the PowerDeviceType for a given scheme.
     * @param scheme The scheme.
     * @return The PowerDeviceType.
     * */
    public static PowerDeviceType findType(String scheme){
        PowerDeviceType retVal = null;
        
        for(PowerDeviceType type : PowerDeviceType.values()){
            if(type.getScheme().equals(scheme)){
                retVal = type;
                break;
            }
        }
        
        return retVal;
    }
}
