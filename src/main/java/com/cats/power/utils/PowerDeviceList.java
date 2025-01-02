package com.cats.power.utils;

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

import com.cats.power.model.PowerInfo;

import java.util.ArrayList;

/**
 * An encapsulating util class that encapsulates a List. This is created since
 * {@link @Deprecated PowerBinder} had to inject a List and encountered problems due to Type
 * erasure.
 */
public class PowerDeviceList extends ArrayList<PowerInfo>
{
    private static final long serialVersionUID = -7726778514041011603L;
}
