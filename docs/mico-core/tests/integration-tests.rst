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

Integration tests are marked with the annotation :java:`@Category(IntegrationTests.class)`

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

Run locally
-----------

To run the integration tests locally execute

.. code:: bash

    mvn failsafe:integration-test
