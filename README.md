# No Fault Divorce Case API
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Anfdiv-case-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Anfdiv-case-api) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Anfdiv-case-api&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Anfdiv-case-api) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Anfdiv-case-api&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Anfdiv-case-api) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=uk.gov.hmcts.reform%3Anfdiv-case-api&metric=coverage)](https://sonarcloud.io/summary/new_code?id=uk.gov.hmcts.reform%3Anfdiv-case-api)

This Spring Boot API handles callbacks from CCD for the NFD case type......

## Overview

<p align="center">
<a href="https://github.com/hmcts/nfdiv-frontend">nfdiv-frontend</a> • <b><a href="https://github.com/hmcts/nfdiv-case-api">nfdiv-case-api</a></b> • <a href="https://github.com/hmcts/nfdiv-shared-infrastructure">nfdiv-shared-infrastructure</a> • <a href="https://github.com/hmcts/nfdiv-performance">nfdiv-performance</a> • <a href="https://github.com/hmcts/nfdiv-solicitor-e2e-tests">nfdiv-solicitor-e2e-tests</a>
</p>

<br>

<p align="center">
  <img src="https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/c4/nfdiv/images/structurizr-nfdiv-overview.png" width="700"/>
</p>

<details>
<summary>Citizen view</summary>
<img src="https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/c4/nfdiv/images/structurizr-nfdiv-citizen.png" width="600">
</details>
<details>
<summary>Caseworker view</summary>
<img src="https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/c4/nfdiv/images/structurizr-nfdiv-caseworker.png" width="600">
</details>

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

    ./gradlew build

### Running the application locally

You will need access to the nfdiv-aat vault and an active VPN to run locally as it depends on services in AAT.

Run the application by executing the following command:

    ./gradlew bootRun

This will start the API container exposing the application's port
(set to `4013` in this template app).

In order to test if the application is up, you can call its health endpoint:

    curl http://localhost:4013/health

You should get a response similar to this:

    {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}

### Running the application locally with CCD and XUI

If you would like to run the full CCD and XUI stack locally you can use:

    ./gradlew bootWithCcd

Then you can access XUI on `http://localhost:3000`

### Generate CCD JSON files

Generating the CCD JSON files will happen on every `./gradlew bootWithCcd` but you can manually trigger this with:

    ./gradlew generateCCDConfig

### Generate TypeScript definitions for CCD definition

    ./gradlew generateTypeScript

### Crons

You can manually run a cron task from the cli:

```
TASK_NAME=[task] java -jar nfdiv-case-api.jar run

# E.g.
TASK_NAME=SystemProgressHeldCasesTask java -jar nfdiv-case-api.jar

# or
TASK_NAME=SystemProgressHeldCasesTask ./gradlew bootRun

Before running cron/task locally, these changes are required
Add task name as environment variable
#e.g.
Add TASK_NAME=SystemProgressHeldCasesTask in .aat-env file

By default, task will be run against ccd in aat. In order to run it 
against local ccd, in application.yaml file, change this CASE_DATA_STORE_BASEURL:http://localhost:4452
or add CASE_DATA_STORE_BASEURL=http://localhost:4452 in .aat-env file

If running against local case api, we need to add below to .aat-env for task
to run on a different port than on default port 4453 on which case api runs.

Without below step it will throw port in use exception.
Add SERVER_PORT=[random port] after nfdiv-case-api has started in local.
```

## License 

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
