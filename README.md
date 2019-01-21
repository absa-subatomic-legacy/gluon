# Gluon  [![Build Status](https://travis-ci.org/absa-subatomic/gluon.svg?branch=master)](https://travis-ci.org/absa-subatomic/gluon) [![codecov](https://codecov.io/gh/absa-subatomic/gluon/branch/master/graph/badge.svg)](https://codecov.io/gh/absa-subatomic/gluon) [![Maintainability](https://api.codeclimate.com/v1/badges/b7ab83c942404ff6fa90/maintainability)](https://codeclimate.com/github/absa-subatomic/gluon/maintainability)

Gluon stores and gives context in the exchange between Quantum Mechanic and Atomist. It also emits events to Atomist in
response to data changes. 

> A gluon is an elementary particle that acts as the exchange particle for the strong force between quarks. It is analogous 
to the exchange of photons in the electromagnetic force between two charged particles. - [Wikipedia](https://g.co/kgs/tuyx3j)

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing 
purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
Refer to the [Local-Hadron-Colider](https://github.com/absa-subatomic/local-hadron-collider) and 
             [Quantum-Mehanic](https://github.com/absa-subatomic/quantum-mechanic) which should be setup in order to 
             use Gluon

### Installing

1. Open a terminal
2. Clone the repo to your local drive.
```console
 git clone https://github.com/absa-subatomic/gluon.git
 ``` 
3. Navigate to the folder containing the code 
4. Run the the following command in the same terminal to run and test installation
```console
$ ./mvnw spring-boot:run -pl nucleus -Dspring.profiles.active=local
```

### Development Setup

To run Gluon locally using an in memory H2 database:

```console
$ ./mvnw spring-boot:run -pl nucleus -Dspring.profiles.active=local
```

#### `local` Spring profile

The local Spring profile can be setup by following the instructions [here](nucleus/src/etc/atomist-config/README.md).
To successfully invoke the Atomist webhooks, you **must complete the steps above** before starting Gluon.

By default Gluon will be available at: http://localhost:8080

#### Docker

To build a Docker image with a runnable build of Gluon, you can use the S2I tool.

##### S2I

[Source-to-Image (S2I)](https://github.com/openshift/source-to-image)
is a toolkit and workflow for building reproducible Docker images from source code.

First install S2I by following the [Installation](https://github.com/openshift/source-to-image#installation)
instructions.

Then in the root directory of Gluon run:

```console
$ s2i build . absasubatomic/s2i-jdk8-maven3-subatomic subatomic-gluon --env ARTIFACT_DIR=nucleus/target
...
```

note the use of the `ARTIFACT_DIR` environment variable which is needed as Gluon is a multi module
Maven project. See the base [RHEL S2I Java builder image](https://github.com/fabric8io-images/s2i/tree/master/java/images/rhel#build-time)
for more information.

Alternatively, using the GitHub codebase, in any directory run:

```console
$ s2i build https://github.com/absa-subatomic/gluon.git absasubatomic/s2i-jdk8-maven3-subatomic subatomic-gluon --env ARTIFACT_DIR=nucleus/target
...
```

##### Running the Docker image

Once the S2I build has completed, you can run Gluon with:

```console
$ docker run -p 8080 subatomic-gluon
...
```

## Running the tests

### Postman

There are Postman collections located in the `nucleus/src/etc/postman` directory.
These can be imported and used to test the available API resources.

See the Postman [reference documentation](https://www.getpostman.com/docs/postman/collections/data_formats)
for more information.

## Deployment
Add additional notes about how to deploy this on a live system

Preferred method is to run in a Docker image. Refer to note on Docker under development.

## Contributing
Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of 
conduct, and the process for submitting pull requests to us.

## Versioning
No versioning is in place at the time of creating this documentation.

## Authors
[Kieran Bristow](https://github.com/kbristow), 
[Kieran Bester](https://github.com/KieranHons),
[Donovan Muller](https://github.com/donovanmuller),
[Andre de Jager](https://github.com/andrejonathan),
[Chris Kieser](https://github.com/chriskieser)

## License
This project is licensed under the Apache License v2.0 - see the 
[LICENSE](https://github.com/absa-subatomic/gluon/blob/Update-readme.md/LICENSE) file for details

<!-- Placeholder for future if required
## Acknowledgments
Hat tip to anyone whose code was used
Inspiration 
-->
