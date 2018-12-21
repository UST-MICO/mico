
[![Build Status](https://travis-ci.org/UST-MICO/mico.svg?branch=master)](https://travis-ci.org/UST-MICO/mico)

# MICO

> A Management System for Microservice Compositions

This is the main repository for the development project MICO at the University of Stuttgart in the masters course Software Engineering.

## Docker Setup

The fastest way to get the backend of our system up and running is to use Docker.
The docker-compose.yml file includes neo4j and builds our backend. Simply run:
```
docker-compose up
```
The script `insertTestValues.sh` contains sample values and posts them to our backend.

## Documentation

* [USERS](https://mico-docs.readthedocs.io) 
* [CONTRIBUTORS](https://mico-dev.readthedocs.io)
