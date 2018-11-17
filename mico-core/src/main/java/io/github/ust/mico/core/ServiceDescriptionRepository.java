package io.github.ust.mico.core;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "serviceDescribtions", path = "serviceDescribtions")
public interface ServiceDescriptionRepository extends PagingAndSortingRepository<ServiceDescription, Long> {
}
