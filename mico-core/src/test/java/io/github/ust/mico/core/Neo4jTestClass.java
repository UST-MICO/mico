package io.github.ust.mico.core;

import org.junit.After;
import org.junit.Before;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class Neo4jTestClass {

    @Autowired
    Session session; //TODO: Remove Warning 'Could not autowire. No beans of 'Session' type found.'

    @Before
    @Transactional
    public void setUp() {
        //TODO: Implementation
    }

    @After
    @Transactional
    public void tearDown() {
        session.purgeDatabase();
    }
}
