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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;

public class PackageClassScanner {
    /**
    * Scans all classes accessible from the context class loader which belong
    * to the given package and subpackages.
    * 
    * @return The classes
    * @throws ClassNotFoundException
    * @throws IOException
    */
    public static List<Class> getAnnotatedClasses(Class annotationClazz) throws ClassNotFoundException, IOException
    {
        Set<Class<?>> classSet;
        Reflections reflections = new Reflections("com.cats");
        classSet = reflections.getTypesAnnotatedWith(annotationClazz);
        List<Class> reconClasses = new ArrayList<>(classSet);
        return reconClasses;
    }
    
    public static List<Method> getAnnotatedMethods(List<Class> classes, Class annotationClazz){
        List<Method> methods = new ArrayList<>();
        for(Class clazz: classes){
            for(Method method : clazz.getDeclaredMethods()){
                if(method.isAnnotationPresent(annotationClazz)){
                    methods.add(method);
                }
            }
        }
        return methods;
    }
}
