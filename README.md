##Login
`gcloud auth login`

##run project
`gcloud config set project bbqb-prd`
`./mvnw -DskipTests package appengine:deploy -Dapp.deploy.version=local -Dapp.deploy.promote=False`
