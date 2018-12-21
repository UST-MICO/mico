
[![Build Status](https://travis-ci.org/UST-MICO/mico.svg?branch=master)](https://travis-ci.org/UST-MICO/mico)

# MICO

> A Management System for Microservice Compositions

This is the main repository for the development project MICO at the University of Stuttgart in the masters course Software Engineering.

## Docker Setup

The fastest way to get the MICO backend up and running is to use Docker.
The `docker-compose.yml` file includes a Neo4j graph database and builds the backend.
Simply run:
```
docker-compose up
```
The script `insertTestValues.sh` contains sample values and adds them to the database.

## Documentation

* [USERS](https://mico-docs.readthedocs.io) 
* [CONTRIBUTORS](https://mico-dev.readthedocs.io)
