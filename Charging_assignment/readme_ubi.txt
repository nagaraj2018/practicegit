# Coding challenge
**Car park Ubi**

List of services: [attached post collection used for testing]


#1 for creating the carpark.
POST http://localhost:3002/carpark/
Request:
{
    "id": "UB1",
    "name": "UB1 Car Parking",
    "description": "UB1 Car Parking desc",
    "chargingCapacity": 100
}

#2 For getting the carpark
GET http://localhost:3002/carpark

Response:
[
    {
        "id": "UB1",
        "name": "UB1 Car Parking",
        "description": "UB1 Car Parking desc",
        "chargingCapacity": 100
    }
]

#3 for getting the list of charging points
GET http://localhost:3002/carpark/UB1/chargingpoints

Response:

[
    {
        "id": "CP1",
        "name": "ChargingPoint#1",
        "description": "ChargingPoint#1",
        "chargingQty": 0,
        "inUse": 0,
        "chargeStartTime": null,
        "carPark": {
            "id": "UB1",
            "name": "UB1 Car Parking",
            "description": "UB1 Car Parking desc",
            "chargingCapacity": 100
        }
    }
]

#4 for update the Chargepoint as Plugin
PUT http://localhost:3002/carpark/UB1/chargingpoints/CP1
{
"id":"CP1",
"name":"ChargingPoint#1",
"description":"ChargingPoint#1",
"inUse":1
}
repeat for different inputs: CP2 to CP10
PUT http://localhost:3002/carpark/UB1/chargingpoints/CP2
{
"id":"CP2",
"name":"ChargingPoint#1",
"description":"ChargingPoint#1",
"inUse":1
}

#4 for update the Chargepoint as UnPlugin
PUT http://localhost:3002/carpark/UB1/chargingpoints/CP1
{
"id":"CP1",
"name":"ChargingPoint#1",
"description":"ChargingPoint#1",
"inUse":0
}

repeat for different inputs: CP2 to CP10
PUT http://localhost:3002/carpark/UB1/chargingpoints/CP2
{
"id":"CP2",
"name":"ChargingPoint#1",
"description":"ChargingPoint#1",
"inUse":0
}

#5 for getting the available capacity
 http://localhost:3002/carpark/UB1/chargingpointsCapAvailable
 
 #6 for getting specific charge point
GET http://localhost:3002/carpark/UB1/chargingpoints/CP1

{
    "id": "CP1",
    "name": "ChargingPoint#1",
    "description": "ChargingPoint#1",
    "chargingQty": 0,
    "inUse": 0,
    "chargeStartTime": null,
    "carPark": {
        "id": "UB1",
        "name": "UB1 Car Parking",
        "description": "UB1 Car Parking desc",
        "chargingCapacity": 100
    }
}

#7 CarParkApplicationTests.java has Junit Integration testcases for below flows.

getChargingPoints
addChargingPointInSequence
removeChargingPointInSequence

List of improvements:
Error handling and return of error codes.
Logging using frameworks.
Granular Junit testcases can be added at unit and integration level
