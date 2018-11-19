package io.github.ust.mico.core;


import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "dependsOns", path = "dependsOns")
public interface DependsOnRepository extends PagingAndSortingRepository<DependsOn, Long> {
}
