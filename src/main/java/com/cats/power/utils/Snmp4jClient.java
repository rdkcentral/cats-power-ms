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

import com.cats.power.exceptions.SNMPException;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * This is a possible try to keep it small and simple for use to power devices.
 */
public class Snmp4jClient
{

    /**
     * SNMP GET Request, responds in string
     * @param oId
     * @param communityName
     * @param targetIP
     * @param portNumber
     * @return
     * @throws SNMPException
     * @throws IOException
     */
    public static String get(String oId, String communityName, String targetIP, int portNumber) throws SNMPException, IOException
    {
        Snmp snmp = new Snmp();
        start(snmp);
        Target target = getTarget(targetIP, portNumber, communityName);
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oId)));
        pdu.setType(PDU.GET);

        return processResponse(snmp.get(pdu, target));

    }

    /**
     * SNMP GET Request, responds in string
     * @param oId
     * @param communityName
     * @param targetIP
     * @param portNumber
     * @param command
     * @return
     * @throws Exception
     */
    public static String set(String oId, String communityName, String targetIP, int portNumber, String command)
            throws SNMPException, IOException
    {

        Snmp snmp = new Snmp();
        start(snmp);
        Target target = getTarget(targetIP, portNumber, communityName);
        final Integer intValue = Integer.parseInt(command);
        final Integer32 setInteger32Value = new Integer32(intValue);
        PDU pdu = new PDU();
        VariableBinding varbinding = new VariableBinding(new OID(oId), setInteger32Value);
        pdu.add(varbinding);
        pdu.setType(PDU.SET);
        return processResponse(snmp.set(pdu, target));
    }

    /**
     * @param snmp
     * @throws IOException
     */
    private static void start(Snmp snmp) throws IOException
    {
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp.addTransportMapping(transport);
        transport.listen();
    }

    /**
     * This method returns a Target, which contains information about where the data should be fetched and how.
     * @return
     */
    private static Target getTarget(String targetIp, int portNumber, String communityName)
    {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(communityName));
        target.setAddress(new UdpAddress(targetIp + "/" + portNumber));
        target.setRetries(2);
        target.setTimeout(2000);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    /**
     * Process the SNMP response
     * @param event
     * @return {@link ResponseEvent}
     * @throws SNMPException
     */
    private static String processResponse(ResponseEvent event) throws SNMPException
    {
        if (event != null)
        {
            PDU responsePDU = event.getResponse();

            if (responsePDU != null)
            {
                if (responsePDU.getErrorStatus() == PDU.noError)
                {
                    return responsePDU.getVariableBindings().get(0).toString();

                }
                else
                {
                    throw new SNMPException(" Found PDU ERROR details : PDU ERROR STATUS : " + responsePDU.getErrorStatus()
                            + " PDU ERROR INDEX : " + responsePDU.getErrorIndex() + " PDU ERROR TEXT : "
                            + responsePDU.getErrorStatusText());
                }
            }
            else
            {
                throw new SNMPException(" Response PDU is null ");
            }
        }
        else
        {
            throw new SNMPException(" SNMP request timedout, no response recieved ");
        }
    }

}
