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

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

/**
 * HttpClientUtil class sends commands to the server via HTTP.
 * */
@Component
@Slf4j
public class HttpClientUtil {

    @Autowired
    MeasuredHttpClientUtil measuredHttpClientUtil;

    /**
     * Transmits a command to the server.
     * @param buildUrl
     * @param host
     * @param portIp
     * @param username
     * @param password
     * @return
     */
    public String transmit(String buildUrl, String host, int portIp, String username, String password) {
        int retries = 0;
        String response = null;
        do {
            try {
                HttpGet dhttpget = new HttpGet(buildUrl);
                response = transmit(dhttpget, host, portIp, username, password);
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("Exception [{}]", e.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    log.debug("Sleep interrupted");
                }
                retries++;
            }
        } while (null == response && retries < 3);
        return response;
    }

    /**
     * Transmits a command to the server with authentication.
     * @param buildUrl
     * @param host
     * @param portIp
     * @param username
     * @param password
     * @return
     */
    public String transmitWithAuth(String buildUrl, String host, int portIp, String username, String password) {

        HttpGet dhttpget = new HttpGet(buildUrl);
        byte[] encodedCredentials = Base64.getEncoder().encode((String.format("%s:%s", username, password)).getBytes());
        String headerCredentials = String.format("Basic %s", new String(encodedCredentials));
        Header header = new BasicHeader("authorization", headerCredentials);
        dhttpget.setHeader(header);
        return transmit(dhttpget, host, portIp, username, password);
    }

    /**
     * Transmits a command to the server.
     * @param httpRequestBase
     * @param host
     * @param portIp
     * @param username
     * @param password
     * @return
     */
    public String transmit(HttpRequestBase httpRequestBase, String host, int portIp, String username, String password) {
        HttpResponse httpResponse = null;
        String response = null;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(1000)
                .setConnectionRequestTimeout(1000)
                .build();

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(host, portIp), new UsernamePasswordCredentials(username, password));

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        try {

            httpResponse = measuredHttpClientUtil.measuredExecute(httpclient,httpRequestBase);

            log.info("POWER REQUEST Url=[{}] ", httpRequestBase.getURI());
            log.info("POWER REQUEST STATUS [{}]", httpResponse.getStatusLine().getStatusCode());
            response = EntityUtils.toString(httpResponse.getEntity());
            ;
        } catch (Exception e) {
            log.warn("Exception [{}]", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.warn("Exception [{}]", e.getMessage());
            }
        }
        return response;
    }
}
