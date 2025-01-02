# Power Microservice

The Power Microservice supports hard-power on/off operations for devices under test. To achieve this, the Power Microservice interfaces with programmable power distribution units (PDUs) deployed to the rack. A device under test is directly connected an individual outlet on the PDU, allowing for commands to enable control of Power ON, Power OFF, and Power REBOOT for individual devices under test. Custom slot mapping is supported to ensure the microservice is aware of all devices under test connected to each PDU deployed to the rack and the individual outlet assignments for each device.

<br><br>

## Development Setup

Build using `mvn clean install`

Run using `java -jar target/power-ms.jar`   


### Running Locally

`mvn spring-boot:run`   


Once running, application will be locally accessible at http://localhost:9090/power/


<br><br>


## Building

Build the project using `mvn clean install`.
Copy the built jar file into the corresponding directory structure as required by the Dockerfile.

    docker build -t="/powerms" .


<br><br>


## Deploying

Copy the `application.yml` file to the `/opt/data/powerms`.
Specify the host and port for the power devices this microservice will service in the prod.yml file

    powerDevices:
      - host: 192.168.100.21
        port: 80
      - host: 192.168.100.22
        port: 80

Also provide environment variable `POWER_LOG` where specifies where log files are required.

<br>

Example prod.yml file for Synaccess hardware devices:

    powerDevices:
        - host: 192.168.100.21
          port: 80
          maxPort : 2
          deviceId : 1
          type : synaccess
        - host: 192.168.100.22
          port: 80
          maxPort : 2
          deviceId : 2
          type : synaccess



<br><br>


## NGINX Configuration

NGINX is used to support a unified path for communication to the rack microservices as well as communication between the rack microservices. NGINX configuration for power-ms can be found at [power-ms.conf](conf/power-ms.conf). This configuration file is used to route requests to the power microservice.


<br><br>


## Supported Power Device Hardware 

Each power device specified in the config.yml file must also include a type. The currently supported type(s) are listed below:


| Hardware Name | Hardware Type Identifier | Connection Protocol | Additional Details                                       | Documentation                                                                                                                                                                                                   |
| --- | --- |-------------------|----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|Synaccess|synaccess| HTTP & SNMP       |                                                          | [Synaccess Docs](https://www.synaccess-net.com/support?p=documentation)                                                                                                                                         |
|Eaton G3|eatonG3| SNMP              | Eaton G3 and other Eaton devices utilizing same SNMP MIB | [Eaton G3 Docs](https://www.eaton.com/content/dam/eaton/products/backup-power-ups-surge-it-power-distribution/power-distribution-for-it-equipment/eaton-basic-rack-pdu/Eaton_ePDU_G3_Operations_Manual.133.pdf) |
|Digital Loggers Web Power Switch|digitalLogger| HTTP              | Older Hardware                                           | [Digital Loggers Web Power Switch Docs](https://www.digital-loggers.com/lpc2man.pdf)                                                                                                                            |
|Digital Loggers Web Power Switch Pro|digitalLogger| HTTP (REST)       | Uses proper REST interface                               | [Digital Loggers Web Power Switch Pro Docs](https://www.digital-loggers.com/proman.pdf)                                                                                                                         |
|Raritan|raritanPX35145R| SNMP              | Outlet metered IP PDU from Raritan                       | [Raritan Docs](https://cdn1.raritan.com/download/pdu-g2/4.2.10/PX2_PX3_QSG_B1_4.1.0.pdf)                                                                                                                        |
|Lindy IPower Switch Classic|lindy| SNMP              | Simple Lindy IPower Switch Classic used in UK            | [Lindy Docs]()                                                                                                                                                                                                  |

For more information have a look at the type definitions in [PowerDeviceType.java](src/main/java/com/cats/power/service/PowerDeviceType.java).


<br><br>


## Access the Swagger Documentation

The Swagger Documentation for the Power Microservice can be accessed at https://localhost:9090/power/swagger-ui.html when running locally. Default swagger path is **/power/swagger-ui.html**.


<br><br>


## Custom Power Requests

Certain REST calls in the microservice accept a request body that contains a snapshot
of the desired system state to achieve. These request bodies leverage the same
contract used when requesting state information from the microservice. Each of these
requests has an optional query parameter `returnState`. If `returnState` is set
to false or not included, a successful custom power request will return `204 NO CONTENT`
otherwise a snapshot of the most recent state of the system after the request was
satisfied will be returned.

There are three custom request calls: slot, device, and all devices. Each request
specifies the outlet or slot to control and the status to set the slot to.
The accepted slot statuses are: `ON, OFF, REBOOT, IGNORE`. Any entity not included
in the request is ignored.


<br><br>


## Custom Slot Mapping

Power-ms offers the capability to customize any slot's device and outlet reference.
This allows for flexibility in slot capability for non-traditional rack deployments.
For instance, say you have a single 8 outlet power device and 9 slots on your rack.
If you want device 9 to have power capability but it is not necessary for device 2,
you could create a slot mapping that allows for this with the following JSON:

    {
        "slots": {
            "1": "1:1",
            "3": "1:2",
            "4": "1:3",
            "5": "1:4",
            "6": "1:5",
            "7": "1:6",
            "8": "1:7",
            "9": "1:8"
        }   
    }

This would be stored as `mappings.json` in the `/powerms` directory by default.


<br><br>


### Power Health Check

    GET http://localhost:9090/power/actuator/health 

