package io.github.ust.mico.core;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "applications", path = "applications")
public interface ApplicationRepository extends PagingAndSortingRepository<Application, Long> {
}
