package com.cats.power.device;

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

import com.cats.power.exceptions.UnableToCreatePowerControllerDevice;
import com.cats.power.config.CustomApplicationContext;
import com.cats.power.utils.MeasuredSnmpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.cats.power.utils.PowerConstants;

/**
 * SNMP Power Controller Device is a class that extends PowerControllerDevice
 * and provides implementation for SNMP power controller device.
 * */
public abstract class SnmpPowerControllerDevice extends PowerControllerDevice
{

    private final Logger       log             = LoggerFactory.getLogger( SnmpPowerControllerDevice.class );

    protected final static int DEFAULT_PORT    = 161;
    protected final static int DEFAULT_TIMEOUT = 2000;
    protected final static int DEFAULT_RETRIES = 2;


    protected Snmp             snmp;
    protected CommunityTarget  target;

    protected Integer          reqId           = 456;

    public SnmpPowerControllerDevice( String host, int port, int timeout, int retries )
    {
        super();
        log.info( "New {} {} {} ", SnmpPowerControllerDevice.class.getSimpleName(), host, port );
        powerInfo.setId(host);
        powerInfo.setPort(port);
        TransportMapping transport;
        try
        {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp( transport );
            Address add = new UdpAddress( host + "/" + port );
            target = new CommunityTarget();
            target.setAddress( add );
            target.setTimeout( timeout );
            target.setRetries( retries );
            target.setCommunity( new OctetString( "public" ) );
            target.setVersion( SnmpConstants.version2c );
        }
        catch ( IOException e )
        {
            throw new UnableToCreatePowerControllerDevice( e );
        }
    }

    public SnmpPowerControllerDevice( String host, int port ) throws IOException
    {
        this( host, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES );
    }

    public SnmpPowerControllerDevice( String host ) throws IOException
    {
        this( host, DEFAULT_PORT );
    }

    /**
     * @return the host
     */
    protected String getHost()
    {
        return powerInfo.getIp();
    }

    /**
     * @param host
     *            the host to set
     */
    protected void setHost( String host )
    {
        powerInfo.setIp(host);
    }

    @Override
    public void createPowerDevConn()
    {
        log.info( "Connect {} {} {} ", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort() );
        try
        {
            snmp.listen();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy()
    {
        log.info( "Destroy {} {} {} ", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort() );
        try
        {
            snmp.close();
        }
        catch ( IOException e )
        {

        }
    }

    protected boolean transmit( int command, String oidStr )
    {
       return this.transmit(target,command,oidStr);
    }

    protected boolean transmit( CommunityTarget  target, int command, String oidStr  )
    {
        boolean rtn = false;
        PDU request = new PDU();
        request.setType( PDU.SET );
        request.setRequestID( new Integer32( reqId++ ) );

        OID oid = new OID( oidStr );
        request.add( new VariableBinding( oid, new Integer32( command ) ) );

        try
        {
            log.info("request prior" +request);
            MeasuredSnmpClientUtil snmpClientUtil = CustomApplicationContext.getBean(MeasuredSnmpClientUtil.class);
            ResponseEvent responseEvent = snmpClientUtil.measuredExecute(snmp, request,target);
            log.info("response ----" +responseEvent);
            if ( responseEvent != null && responseEvent.getResponse() != null )
            {
                int response = responseEvent.getResponse().get( 0 ).getVariable().toInt();
                if ( response == command )
                {
                    rtn = true;
                }
            }
            else
            {
                rtn = false;
                log.error( "Did not get a response from the power device" );
                //   throw new RuntimeException("Didnot get a response from the device. please check network connections to the device");

            }
        }
        catch ( IOException e )
        {
            rtn=false;
            log.error( "STATUS FAILED {} {} {} {} {} ERROR[{}]", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort(), rtn,
                    e.getMessage() );
            //    throw new RuntimeException("Error Occurred. please check network connections to the device. "+ e.getMessage());
        }
        return rtn;
    }

    protected String queryOutletStatus( String strOid )
    {

        String rtn = PowerConstants.STATUS_UNKNOWN;

        PDU request = new PDU();
        request.setType( PDU.GET );

        OID oid = new OID( strOid );
        request.add( new VariableBinding( oid ) );

        ResponseEvent responseEvent;
        try
        {
            responseEvent = snmp.send( request, target );
            
            if ( responseEvent != null && responseEvent.getResponse() != null )
            {
            	log.info("snmp object  response----+"+responseEvent.getResponse().get( 0 ));
                Integer response = responseEvent.getResponse().get( 0 ).getVariable().toInt();
                log.info("snmp response----+"+response);
                rtn = parseSNMPResponse(response);

                log.debug( "SNMP response error status :: " + responseEvent.getResponse().getErrorStatus() );

            }
            else
            {
                rtn = "Did not get a response";
                log.error( "Did not get a response from the power device" );
            }

        }
        catch ( IOException e )
        {
            log.error( "STATUS FAILED {} {} {} {} {} ERROR[{}]", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort(), rtn,
                    e.getMessage() );
        }
        log.info( "STATUS {} {} {} {} {}", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort(), rtn );
        return rtn;
    }

    protected String queryOID( String strOid )
    {

        String rtn = null;

        PDU request = new PDU();
        request.setType( PDU.GET );

        OID oid = new OID( strOid );
        request.add( new VariableBinding( oid ) );

        ResponseEvent responseEvent;
        try
        {
            responseEvent = snmp.send( request, target );
            if ( responseEvent != null && responseEvent.getResponse() != null )
            {
                String response = responseEvent.getResponse().get( 0 ).getVariable().toString();
                log.debug( "SNMP response error status :: " + responseEvent.getResponse().getErrorStatus() );
                return response;
            }
            else
            {
                rtn = "Did not get a response";
                log.error( "Did not get a response from the power device" );
            }

        }
        catch ( IOException e )
        {
            log.error( "STATUS FAILED {} {} {} {} {} ERROR[{}]", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort(), rtn,
                    e.getMessage() );
        }
        log.info( "STATUS {} {} {} {} {}", this.getClass().getSimpleName(), powerInfo.getIp(), powerInfo.getPort(), rtn );
        return rtn;
    }
    
   

    
    protected abstract String parseSNMPResponse(Integer response);

}
