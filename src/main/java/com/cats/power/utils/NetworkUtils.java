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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Util methods for Network Connection.
 */
public class NetworkUtils
{

    /**
     * Check if the given host is reachable.
     * @param host
     * @return
     */
    public static boolean ping(String host)
    {
        boolean reachable;

        try
        {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -n 1 "+host);
            int returnVal = p1.waitFor();
            reachable = (returnVal==0);
        }
        catch ( IOException | InterruptedException e)
        {
            reachable = false;
        }
        if (reachable == false) {
            try
            {
                Process p2 = java.lang.Runtime.getRuntime().exec("ping -c 1 "+host);
                int returnVal = p2.waitFor();
                reachable = (returnVal==0);
            }
            catch ( IOException | InterruptedException e)
            {
                reachable = false;
            }
        }
        return reachable;
    }


    /**
     * Check if the given URL is reachable.
     * @param urlString
     * @return
     * @throws Exception
     */
    public static boolean checkHttpConnection(String urlString) throws Exception {
        boolean connectable = false;
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout( 3 * 1000 ); //3 sec
            httpConnection.connect();
            if(HttpURLConnection.HTTP_OK == httpConnection.getResponseCode()
                    || HttpURLConnection.HTTP_UNAUTHORIZED == httpConnection.getResponseCode()){
                connectable = true;
            }
            //else{
                //System.out.println("httpConnection.getResponseCode()+\"  \"+httpConnection.getResponseMessage() = " + httpConnection.getResponseCode()+"  "+httpConnection.getResponseMessage());
            //}
        } catch (IOException e) {
            e.printStackTrace();
            connectable = false;
        }
        
        return connectable;
    }
}
