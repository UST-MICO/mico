package io.github.ust.mico.core;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "serviceInterfaces", path = "serviceInterfaces")
public interface ServiceInterfaceRepository extends PagingAndSortingRepository<ServiceInterface, Long> {
}
