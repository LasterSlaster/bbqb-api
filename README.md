##Login
`gcloud auth login`

##run project
`gcloud config set project bbqb-prd`
`./mvnw -DskipTests package appengine:deploy -Dapp.deploy.version=local -Dapp.deploy.promote=False`

## Funktion
- Funktionalität des REST-Service beschreiben

## Development
### Configuration
- GCP Firestore: Das Spring Boot Starter fügt automatisch die Annotation @EnableReactiveFirestoreRepositories hinzu und default configuriert das Projekt für Firestore
- Für die Kommunikation mit der GCP benötigt die Applikation einen Weg sich zu Authentifizieren. Dies kann mit einem Keyfile eines Dienstaccounts als lokales File im Properties File unter `pring.cloud.gcp.credentials.location` angegeben werden oder über die Umgebungsvariable `GOOGLE_APPLICATION_CREDENTIALS` erfolgen.

### Developer
- Marius Degen
- Dimitri Kreik