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

/**
 * DigitalLoggerOutletInfo: Defines Outlet Information specific to a Digital Logger Power Device.
 */
@Schema(name = "DigitalLoggerOutletInfo", description = "Digital Logger Outlet Information")
public class DigitalLoggerOutletInfo {

    /**
     * @return the name of the outlet
     */
    private String name;

    /**
     * @return the locked status of the outlet
     */
    private boolean locked;

    /**
     * @return the critical status of the outlet
     */
    private boolean critical;

    /**
     * @return the transient state of the outlet
     */
    private boolean transient_state;

    /**
     * @return the physical state of the outlet
     */
    private boolean physical_state;

    /**
     * @return the power cycle delay of the outlet
     */
    private int cycle_delay;

    /**
     * @return the state of the outlet
     */
    private boolean state;
   
   public String getName(){
       return name;
   }
   
   public void setName(String name){
       this.name = name;
   }
   
   public boolean getLocked(){
       return locked;
   }
   
   public void setLocked(boolean locked){
       this.locked = locked;
   }
   
   public boolean getCritical(){
       return critical;
   }
   
   public void setCritical(boolean critical){
       this.critical = critical;
   }
   
   public boolean getTransient_state(){
       return transient_state;
   }
   
   public void setTransient_state(boolean transient_state){
       this.transient_state = transient_state;
   }
   
   public boolean getPhysical_state(){
       return physical_state;
   }
   
   public void setPhysical_state(boolean physical_state){
       this.physical_state = physical_state;
   }
   
   public int getCycle_delay(){
       return cycle_delay;
   }
   
   public void setCycle_delay(int cycle_delay){
       this.cycle_delay = cycle_delay;
   }
   
   public boolean getState(){
       return state;
   }
   
   public void setState(boolean state){
       this.state = state;
   }
}