## Login
`gcloud auth login`

## run project
`gcloud config set project bbqb-prd`
`./mvnw -DskipTests package appengine:deploy -Dapp.deploy.version=local -Dapp.deploy.promote=False`

## Funktion
Die App realisiert einen REST-Service als API für CRUD-Operationen zur Kommunikation mit BBQB-Devices. Die API ermöglicht es außerdem Signale an die Devices zu senden.

## Endpoints
- GET /: Test endpoint to check if service is available
- GET /devices: Retrieve all devices 
- GET /devices/{id}: Get a device by its id
- POST /devices: Create a new device. Expects a JSON Body with a device object
- PUT /devices/{id}: Update an existing device or if no device with the specified id exists create a new one at that location. Excpects a JSON body with the device object to update/create. URL-Path id and request body id have to be the same!
- POST /message: Send an open signal to a device to unlock it. Body must include a device object with value id.

### Device Object
{
    "id": "1mdA7jOgGoAj7SKCRouf",
    "deviceId": "1mdA7jOgGoAj7SKCRouf",
    "number": "1",
    "publishTime": 1593253000,
    "status": "idle",
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

## Firestore Database
This service try's to communicate with a gcp firestore to manage device information.
It expects a device document of the following structure:

{
addressName: "HTWG Strandbar" (String)
city: "Konstanz" (String)
country: "idle" (String)
deviceId: "a708a6de-3ca3-4e2e-935a-2998091f033a" (String)
houseNumber: "12" (String)
location: [7° N, 100° E] (Geopunkt)
number: "1212" (String)
postalCode: "78467" (String)
publishTime: 19. Januar 1970 um 11:34:13 UTC+1 (Zeitstempel)
status: "idle" (String)
street: "Konstanzerstraße" (String)
}

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