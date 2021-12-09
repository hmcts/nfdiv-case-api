# No Fault Divorce Case API [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

This API handles callbacks from CCD for the NO_FAULT_DIVORCE case type.

## Overview

                        ┌────────────────┐
                        │                │
                        │ NFDIV-CASE-API │
                        │                │
                        └───────▲────────┘
                                │
                                │
                        ┌───────▼────────┐
                        │                │
                  ┌─────►      CCD       ◄─────┐
                  │     │                │     │
                  │     └────────────────┘     │
                  │                            │
          ┌───────┴─────────┐        ┌─────────┴───────┐
          │                 │        │                 │
          │ NFDIV-FRONTEND  │        │       XUI       │
          │                 │        │                 │
          └─────────────────┘        └─────────────────┘

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

    ./gradlew build

### Running the application

Create the image of the application by executing the following command:

    ./gradlew assemble

Create docker image:

    docker-compose build

Run the distribution (created in `build/install/nfdiv-caseapi` directory)
by executing the following command:

    docker-compose up

This will start the API container exposing the application's port
(set to `4013` in this template app).

In order to test if the application is up, you can call its health endpoint:

    curl http://localhost:4013/health

You should get a response similar to this:

    {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}

### Generate CCD JSON files

To generate the CCD JSON files from the Java Model run the following from the root of the project:

    ./gradlew ccd-definitions:generateCCDConfig

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
```

To configure a new cron in AAT please checkout the [cnp-flux-config](https://github.com/hmcts/cnp-flux-config/) repository and run:

```
./bin/add-cron.sh SystemProgressHeldCasesTask ~/cnp-flux-config "0/10 * * * *"
```

Then create a PR in the cnp-flux-config repository.

Note that the cron will only run in the aat-00 cluster as we don't have a way to run the job once over multiple clusters. Let's hope that cluster doesn't go down.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
