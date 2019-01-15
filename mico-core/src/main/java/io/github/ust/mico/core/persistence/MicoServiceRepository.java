package io.github.ust.mico.core.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import io.github.ust.mico.core.ServiceInterface;
import io.github.ust.mico.core.model.MicoService;

public interface MicoServiceRepository extends Neo4jRepository<MicoService, Long> {

    // TODO: Adapt to new domain model
    @Deprecated
    List<MicoService> findByName(@Param("name") String name);

    // TODO: Adapt to new domain model
    @Deprecated
    List<MicoService> findByShortName(@Param("shortName") String shortName);

    // TODO: Adapt to new domain model
    @Deprecated
    List<MicoService> findByName(@Param("name") String name, @Depth int depth);

    // TODO: Adapt to new domain model
    @Deprecated
    List<MicoService> findByShortName(@Param("shortName") String shortName, @Depth int depth);

    // TODO: Adapt to new domain model
    // TODO: probably move to MicoServiceInterfaceRepository
    @Deprecated
    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} return interface")
    List<ServiceInterface> findInterfacesOfService(@Param("shortName") String shortName, @Param("version") String version);

    // TODO: Adapt to new domain model
    // TODO: probably move to MicoServiceInterfaceRepository
    @Deprecated
    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} return interface")
    Optional<ServiceInterface> findInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    // TODO: Adapt to new domain model
    // TODO: probably move to MicoServiceInterfaceRepository
    @Deprecated
    @Query("MATCH (service:Service)-[:SERVICE_INTERFACES]->(interface:ServiceInterface) WHERE service.shortName = {shortName} AND service.version = {version} AND interface.serviceInterfaceName = {serviceInterfaceName} detach delete interface")
    void deleteInterfaceOfServiceByName(@Param("serviceInterfaceName") String serviceInterfaceName, @Param("shortName") String shortName, @Param("version") String version);

    // TODO: Adapt to new domain model
    @Deprecated
    @Query("match (service:Service) where service.shortName = {shortName} and service.version = {version} delete service")
    void deleteServiceByShortNameAndVersion(@Param("shortName") String shortName, @Param("version") String version);

    // TODO: Adapt to new domain model
    @Deprecated
    @Override
    List<MicoService> findAll();

    // TODO: Adapt to new domain model
    @Deprecated
    Optional<MicoService> findByShortNameAndVersion(String shortName, String version, @Depth int depth);

    // TODO: Adapt to new domain model
    @Deprecated
    Optional<MicoService> findByShortNameAndVersion(String shortName, String version);
    
}
