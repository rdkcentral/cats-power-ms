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

import com.cats.power.service.MeasureTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MeasuredHttpClientUtil class sends measured commands to the server via HTTP.
 * */
@Component
@Slf4j
public class MeasuredHttpClientUtil {


    /**
     * Sends a command to the server.
     * @param httpClient The HTTP client.
     * @param httpRequestBase The HTTP request.
     * @return The HTTP response.
     * */
    @MeasureTime
    public HttpResponse measuredExecute(CloseableHttpClient httpClient, HttpRequestBase httpRequestBase) throws IOException {
         return  httpClient.execute(httpRequestBase);
    }

}
