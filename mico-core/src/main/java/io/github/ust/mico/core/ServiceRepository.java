package io.github.ust.mico.core;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends Neo4jRepository<Service, Long> {

    Service findByName(@Param("name") String name);

    List<Service> findByShortName(@Param("shortName") String shortName);

    Service findByName(String name, @Depth int depth);

    Service findByShortName(String shortName, @Depth int depth);

    Optional<Service> findByShortNameAndVersion(String shortName, String version);

    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} return interface")
    List<ServiceInterface> findInterfacesOfService(@Param("shortName") String shortName,@Param("version")  String version);

    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} return interface")
    Optional<ServiceInterface> findInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName,@Param("version")  String version);

    @Override
    List<Service> findAll();
}
