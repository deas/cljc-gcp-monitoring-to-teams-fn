# 🧪 GCP Monitoring to MS Teams - Clojure(Script) flavoured 🥼

Inspired by [gcp-monitoring-to-teams](https://github.com/Courtsite/gcp-monitoring-to-teams).

![Alt Text](https://media.giphy.com/media/YAlhwn67KT76E/giphy.gif)

## Dev Requirements
- Java (for build, Runtime not quite yet)
- Node JS
- Terraform

## Usage (Target: NodeJS)
Install dependencies
```shell
yarn install || npm install
```
Run dev process
```shell
yarn start || npm start
```
Compile
```shell
yarn run build || npm run build
```
Test
```shell
yarn run test || npm run test
```
REPL (but you most likely want to use your IDE)
```shell
yarn run repl || npm run repl
```
Run service locally
```shell
yarn run serve || npm run serve
```
Send incident to MS Teams with local service:
```shell
TEAMS_WEBHOOK=https://get.this.from.teams
curl -H 'Content-Type: application/json' -d @samples/incident.json "http://localhost:8080?auth-token=s3cr3t&teams-endpoint=${TEAMS_WEBHOOK}"
```
Sending to teams directly
```shell
curl POST -H 'Content-type:application/json' -d @samples/adaptive-incident.json $ENDPOINT
```
Deploy (terraform) - make sure to run build first
```shell
yarn run deploy || npm run deploy
```

## TODO / Known Issues
- `gcp-build` does not work because there is no java on the builders
- build optimization is currently `simple` because `promesa/httpurr` appears to have an issue with `advanced` optimization
- Simplify deployment : One call on fresh source should take care of everything
- Build better tests
- Implement hot reloading for `serve`
- Implement PubSub version
- Finish cljc, do JVM host
