package io.github.ust.mico.core;

import org.junit.After;
import org.junit.Before;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class Neo4jTestClass {

    @Autowired
    private Session session;

    @Before
    public void setUp() {
        //TODO: Implementation
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }
}
