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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {
	
	private final Logger logger = LoggerFactory.getLogger( this.getClass() );
	
	@ResponseStatus( code = HttpStatus.NOT_FOUND )
    @ExceptionHandler( value = SlotMappingException.class )
    public String handleSlotMappingNotFoundException( SlotMappingException e )
    {
        logger.warn( "Exception Caught : " + e.getMessage() );
        return  e.getMessage();
    }
	
	@ResponseStatus( code = HttpStatus.SERVICE_UNAVAILABLE  )
    @ExceptionHandler( value = DeviceUnreachableException.class )
    public String handleDeviceUnreachableException( DeviceUnreachableException e )
    {
        logger.warn( "Exception Caught : " + e.getMessage() );
        return  e.getMessage();
    }
	
	
	@ResponseStatus( code = HttpStatus.SERVICE_UNAVAILABLE  )
    @ExceptionHandler( value = SNMPException.class )
    public String handleSNMPException( SNMPException e )
    {
        logger.warn( "Exception Caught : " + e.getMessage() );
        return  e.getMessage();
    }
	
	@ResponseStatus( code = HttpStatus.EXPECTATION_FAILED  )
    @ExceptionHandler( value = UnableToCreatePowerControllerDevice.class )
    public String handleUnableToCreatePowerControllerDevice( UnableToCreatePowerControllerDevice e )
    {
        logger.warn( "Exception Caught : " + e.getMessage() );
        return  e.getMessage();
    }
	
	 @ResponseStatus( code = HttpStatus.BAD_REQUEST )
	 @ExceptionHandler( value = IllegalArgumentException.class )
	 public String handleIllegalArgumentException( IllegalArgumentException e )
	   {
	        e.printStackTrace();
	        logger.warn( "Exception Caught : " + e.getMessage() );
	        return e.getMessage();
	    }
	 @ResponseStatus( code = HttpStatus.BAD_REQUEST )
	 @ExceptionHandler( value = ArrayIndexOutOfBoundsException.class )
	 public String handleArrayIndexOutOfBoundsException( ArrayIndexOutOfBoundsException e )
	   {
	        e.printStackTrace();
	        logger.warn( "Exception Caught : " + e.getMessage() );
	        return e.getMessage();
	    }
	 @ResponseStatus( code = HttpStatus.BAD_REQUEST )
	 @ExceptionHandler( value = NumberFormatException.class )
	 public String handleNumberFormatException( NumberFormatException e )
	   {
	        e.printStackTrace();
	        logger.warn( "Exception Caught : " + e.getMessage() );
	        return e.getMessage();
	    }
	 
	 @ResponseStatus( code = HttpStatus.BAD_REQUEST )
	 @ExceptionHandler( value = IndexOutOfBoundsException.class )
	 public String handleIndexOutOfBoundsException( IndexOutOfBoundsException e )
	   {
	        e.printStackTrace();
	        logger.warn( "Exception Caught : " + e.getMessage() );
	        return e.getMessage();
	    }
	 
	 @ResponseStatus( code = HttpStatus.INTERNAL_SERVER_ERROR )
	    @ExceptionHandler( value = Exception.class )
	    public String handleException( Exception e )
	    {
	        e.printStackTrace(); // print to console to assist debugging
	        logger.warn( "Exception Caught : " + e.getMessage() );
	        String moreInfo = "";
	        StackTraceElement[] stackTrace = e.getStackTrace();
	        for ( StackTraceElement trace : stackTrace )
	        {
	            if ( trace.getClassName().startsWith( "com.cats" ) )
	            {
	                moreInfo = "\nClass " + trace.getClassName() + "\nMethod: " + trace.getMethodName() + "\nLine:"
	                        + trace.getLineNumber();
	                break;
	            }
	        }
	        return e.getClass() + "  " + e.getMessage() + "  " + moreInfo;
	    }

}
