package io.github.ust.mico.core;

import org.springframework.data.repository.CrudRepository;

public interface ServiceRepository extends CrudRepository<Service, Long> {

        Service findByName(String name);
}
