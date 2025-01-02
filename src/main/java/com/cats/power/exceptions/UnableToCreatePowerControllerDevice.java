package com.cats.power.exceptions;

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
 * Customised Exception for PowerControllerDevice creation failure.
 * */
public class UnableToCreatePowerControllerDevice extends RuntimeException
{

    private static final long serialVersionUID = 3081292895566600045L;

    public UnableToCreatePowerControllerDevice()
    {
        super();
    }

    public UnableToCreatePowerControllerDevice( String message )
    {
        super( message );
    }

    public UnableToCreatePowerControllerDevice( Throwable cause )
    {
        super( cause );
    }

    public UnableToCreatePowerControllerDevice( String message, Throwable cause )
    {
        super( message, cause );
    }
}
