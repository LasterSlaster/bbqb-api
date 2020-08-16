## Login
`gcloud auth login`

## run project
`gcloud config set project bbqb-prd`
`./mvnw -DskipTests package appengine:deploy -Dapp.deploy.version=local -Dapp.deploy.promote=False`

## Funktion
- Funktionalität des REST-Service beschreiben

## Endpoints
- POST /devices: Create a new device doc. Expects a JSON Body with following attributes: id:String, number:String, publishTime:long, status:String, location:String, address:String
- GET /devices: Retrieve all devices docs
- GET /devices/{id}: Get a device doc by its id

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