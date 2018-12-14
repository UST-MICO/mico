package io.github.ust.mico.core;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public abstract class Neo4jTestClass {

    @Autowired
    ApplicationRepository applicationRepository;

    @Autowired
    DependsOnRepository dependsOnRepository;

    @Autowired
    ServiceInterfaceRepository serviceInterfaceRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    Session session; //TODO: Remove Warning 'Could not autowire. No beans of 'Session' type found.'

    @Before
    public void setUp() {
        //TODO: Implementation
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }
}
