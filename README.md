##Login
`gcloud auth login`

##run project
`gcloud config set project bbqb-prd`
`./mvnw -DskipTests package appengine:deploy -Dapp.deploy.version=local -Dapp.deploy.promote=False`

## Development
### Configuration
- GCP Firestore: Das Spring Boot Starter fügt automatisch die Annotation @EnableReactiveFirestoreRepositories hinzu und default configuriert das Projekt für Firestore

### Developer
- Marius Degen
- Dimitri Kreik