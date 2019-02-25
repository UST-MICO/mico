Integration Tests
=================

Integration Tests are implemented with JUnit 4.

.. todo:: Update to JUnit 5

.. hint:: CI/CD pipeline

   The setup of the CI/CD pipeline is described in :doc:`setup/cicd`.

Implementation
--------------

.. role:: java(code)
    :language: java

Integration tests are marked with the annotation :java:`@Category(IntegrationTests.class)`.
To add a new integration test, write a JUnit test and annotate the class with the annotation above.

Special Dockerfiles
-------------------
To run integration tests on Jenkins there exists a special Dockerfile, named `Dockerfile.mico-core.integrationtests`, which runs the integration tests on 3.6-jdk-alpine Docker container.
Build the Dockerfile and run the image to test same as Jenkins does.
The Dockerfile runs all tests with the annotation above using the follwing command:
.. code:: bash

    mvn failsafe:integration-test


Requirements
------------

`ImageBuilderIntegrationTests` require credentials to Docker Hub.
Provide the credentials base64 encoded as environment variables:

* `DOCKERHUB_USERNAME_BASE64`
* `DOCKERHUB_PASSWORD_BASE64`

To encode the username and the password with base64 you could use

.. code:: bash

    echo -n "username" | base64 -w 0
    echo -n "password" | base64 -w 0

There are also integration tests which need some Kubernetes credentials.
At the moment we cannot support this within our integration test environment.
So until fixed, integration tests using Kubernetes won't work.
You can ignore them with adding the :java:`@Ignore` annotation on class level.

Run locally
-----------

To run the integration tests locally execute

.. code:: bash

    mvn failsafe:integration-test
