## Login
`gcloud auth login`

## run project
`gcloud config set project bbqb-prd`

`./mvnw -DskipTests package appengine:deploy -Dapp.deploy.version=local -Dapp.deploy.promote=False`

## Funktion
Die App realisiert einen REST-Service als API für CRUD-Operationen zur Kommunikation mit BBQB-Devices. Die API ermöglicht es außerdem Signale an die Devices zu senden.

## Endpoints
- **GET /:**  
Test endpoint to check if service is available
- **GET /devices:**  
Retrieve all devices 
- **GET /devices/{id}:**  
Get a device by its id
- **POST /devices:**  
Create a new device. Expects a JSON Body with a device object
- **PUT /devices/{id}:**  
Update an existing device or if no device with the specified id exists create a new one at that location. Expects a JSON body with the device object to update/create. URL-Path id and request body id have to be the same other wise response code 422 is returned! If the device object in the request body also contains the attribute "locked" with value "true", an open signal(30min) is send to the BBQB with id "deviceId". If this fails response code 500 is returned!
- **POST /message:**  
Send an open signal to a device to unlock it for 30min. Body must include a device object with value deviceId.

### Device Object
```
{
    "id": "1mdA7jOgGoAj7SKCRouf",
    "deviceId": "butler-2",
    "number": "1",
    "publishTime": 1593253000,
    "locked": true,
    "closed": true,
    "wifiSignal": 80.0,
    "temperaturePlate1": 30.0,
    "temperaturePlate2": 31.0,
    "location": {
        "latitude": 7.0,
        "longitude": 100.0
    },
    "address": {
        "country": "idle",
        "postalcode": "78467",
        "city": "Konstanz",
        "street": "Konstanzerstraße",
        "houseNumber": "12",
        "name": "HTWG Strandbar"
    }
}
```
- Attribute "locked" specifies if the BBQB is locked(true)/unlocked(false) with a Boolean value.
- Attribute "closed" specifies if the drawer of a BBQB is closed(true)/open(false) with a Boolean value.
- Attribute "temperaturePlate1/2" specifies the temperature in C° of plate 1/2 with a Double value. 
- Attribute "publishTime" specifies the timestamp of the latest update with the information of a bbqb heartbeat. It's in the format of an integer representing a UTC timestamp in milliseconds.

## Firestore Database
This service communicates with a gcp firestore to manage device information.
It expects a device document with the following structure:

```
{
    id: "a708a6de-3ca3-4e2e-935a-2998091f033a" (String)
    deviceId: "butler-2" (String)
    number: "1212" (String)
    publishTime: 19. Januar 1970 um 11:34:13 UTC+1 (Zeitstempel)
    locked: true (Boolean)
    closed: true (Boolean)
    wifiSignal: 80.0 (Number)
    temperaturePlate1: 30.0 (Number)
    temperaturePlate2: 31.0 (Number)
    location: [7° N, 100° E] (Geopunkt)
    addressName: "HTWG Strandbar" (String)
    street: "Konstanzerstraße" (String)
    houseNumber: "12" (String)
    country: "idle" (String)
    postalCode: "78467" (String)
    city: "Konstanz" (String)
}
```

## Development
### Configuration
- Für die Kommunikation mit der GCP benötigt die Applikation einen Weg sich zu Authentifizieren. Dies kann mit einem Keyfile eines Dienstaccounts als lokales File im Properties File unter `pring.cloud.gcp.credentials.location` angegeben werden oder über die Umgebungsvariable `GOOGLE_APPLICATION_CREDENTIALS` erfolgen.

### Firestore Emulator
Ein Firestore Emulator kann über das google cloud sdk gcloud gestartet werden. Hierzu muss der firestore emulator installiert werden.
Im Properties File müssen zusätzlich noch folgende Werte entsprechend gesetzt werden: "spring.cloud.gcp.firestore.emulator.enabled=true" und "spring.cloud.gcp.firestore.host-port=127.0.0.1:8330"
Zusätzlich sollte in der gcloud das Projekt bbqb-prd gesetzt werden: "gcloud config set project bbqb-prd"
Nach der Installation kann der Emulator mit dem Kommando "gcloud beta emulators firestore start gestartet werden"

### Developer
- Marius Degen
- Dimitri Kreik