# Gluon  [![Build Status](https://travis-ci.org/absa-subatomic/gluon.svg?branch=master)](https://travis-ci.org/absa-subatomic/gluon) [![codecov](https://codecov.io/gh/absa-subatomic/gluon/branch/master/graph/badge.svg)](https://codecov.io/gh/absa-subatomic/gluon) [![Maintainability](https://api.codeclimate.com/v1/badges/b7ab83c942404ff6fa90/maintainability)](https://codeclimate.com/github/absa-subatomic/gluon/maintainability)

A supporting domain applying logic and emitting events to indicate indicate desired state.

> A gluon is an elementary particle that acts as the exchange particle for the strong force between quarks. It is analogous to the exchange of photons in the electromagnetic force between two charged particles. - [Wikipedia](https://g.co/kgs/tuyx3j)

## Development setup

To run Gluon locally using an in memory H2 database:

```console
$ ./mvnw spring-boot:run -pl nucleus --spring.profiles.active=local
```

The local spring profile can be setup by following the instructions [here](nucleus/src/etc/atomist-config/README.md).

By default Gluon will be available at: http://localhost:8080

### Postman

There are Postman collections located in the `nucleus/src/etc/postman` directory.
These can be imported and used to test the available API resources.

See the Postman [reference documentation](https://www.getpostman.com/docs/postman/collections/data_formats)
for more information.
