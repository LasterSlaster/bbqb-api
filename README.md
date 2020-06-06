## Login
`gcloud auth login`

## run project
`gcloud config set project bbqb-prd`
`./mvnw -DskipTests package appengine:deploy -Dapp.deploy.version=local -Dapp.deploy.promote=False`

## Endpoints
- POST /devices: Create a new device doc. Expects a JSON Body with following attributes: id:String, number:String, publishTime:long, status:String, location:String, address:String
- GET /devices: Retrieve all devices docs
- GET /devices/{id}: Get a device doc by its id

## Development
### Configuration
### Firestore Emulator
Ein Firestore Emulator kann über das google cloud sdk gcloud gestartet werden. Hierzu muss der firestore emulator installiert werden.
Im Properties File müssen zusätzlich noch folgende Werte entsprechend gesetzt werden: "spring.cloud.gcp.firestore.emulator.enabled=true" und "spring.cloud.gcp.firestore.host-port=127.0.0.1:8330"
Zusätzlich sollte in der gcloud das Projekt bbqb-prd gesetzt werden: "gcloud config set project bbqb-prd"
Nach der Installation kann der Emulator mit dem Kommando "gcloud beta emulators firestore start gestartet werden"

### Developer
- Marius Degen
- Dimitri Kreik