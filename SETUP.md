# HowTo

Here, you will find instructions on how to setup your local system.

## Neo4j Graph Platform

1. Download **Neo4j Desktop** from https://neo4j.com/download/.
> **Note:** Providing your credentials is mandatory.
2. Install it.
3. Start *Neo4j Desktop*.
4. Create a new project or select default one.
5. Create a new Neo4j graph database by clicking *Add Graph*.
> **Note:** Please select *Create a Local Graph*.
> **Note:** Selecting a password is mandatory.
6. Disable the passwort authentication for your database:
   1. Select *Manage*.
   1. Select *Settings* tab.
   1. Search for `dbms.security.auth_enabled` and set it to `false`.
   1. Select *Apply*.
7. Start the database (see on top in Neo4j Desktop App).
> **Note:** In the pop-up window you need to select *Continue Anyway*.
8. You can connect to your database via the **Neo4j Browser** (see Neo4j Desktop App) or via your **Web Browser** (http://localhost:7474).
