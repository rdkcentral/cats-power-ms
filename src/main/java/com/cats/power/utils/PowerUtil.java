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

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerUtil
{
    /**
     * Regex to parse the outlet number from a URL.
     */
    private static final Pattern OUTLET_PATTERN = Pattern.compile( ".*port=([0-9]+).*" );

    /**
     * Parse the path to find the output to use.
     * 
     * @param path The path to parse
     * @return The outlet requested
     */
    public static int parseOutlet( final URI path )
    {
        final Matcher m = OUTLET_PATTERN.matcher( path.getQuery() );
        if ( !m.find() )
        {
            throw new IllegalArgumentException( "The power outlet must be specified" );
        }
        final String outletStr = m.group( 1 );
        return Integer.parseInt( outletStr );
    }
}
