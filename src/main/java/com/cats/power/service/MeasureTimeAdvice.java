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

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;

@Aspect
@Component
@Slf4j
public class MeasureTimeAdvice {
    /**
     * MeasureTimeAdvice class measures the time taken by a method to execute.
     * */
    @Autowired
    private HttpServletResponse httpServletResponse;

    @Around("@annotation(com.cats.power.service.MeasureTime)")
    public Object measureTime(ProceedingJoinPoint point) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        Instant  startTime = Instant.now();
        stopWatch.start();
        Object object = point.proceed();
        stopWatch.stop();
        try {
            httpServletResponse.setHeader("HW-Command-Request-Time", String.valueOf(startTime));
            httpServletResponse.setHeader("HW-Command-Response-Time", String.valueOf(Instant.now()));
            httpServletResponse.setHeader("HW-Command-Duration-Ms", String.valueOf(stopWatch.getTotalTimeMillis()));
        }catch(IllegalStateException e){
            // not a request with a servlet response. Maybe an internal call. ignore and move on.
        }
        log.info("Time take by " + point.getSignature().getName() + "() method is "
                + stopWatch.getTotalTimeMillis() + " ms");
        return object;
    }
}
