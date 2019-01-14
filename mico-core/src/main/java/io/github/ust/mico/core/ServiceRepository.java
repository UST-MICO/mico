package io.github.ust.mico.core;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends Neo4jRepository<Service, Long> {

    List<Service> findByName(@Param("name") String name);

    List<Service> findByShortName(@Param("shortName") String shortName);

    List<Service> findByName(@Param("name") String name, @Depth int depth);

    List<Service> findByShortName(@Param("shortName") String shortName, @Depth int depth);

    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} return interface")
    List<ServiceInterface> findInterfacesOfService(@Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} return interface")
    Optional<ServiceInterface> findInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} detach delete interface")
    void deleteInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    @Override
    List<Service> findAll();

    Optional<Service> findByShortNameAndVersion(String shortName, String version, @Depth int depth);

    Optional<Service> findByShortNameAndVersion(String shortName, String version);
}
