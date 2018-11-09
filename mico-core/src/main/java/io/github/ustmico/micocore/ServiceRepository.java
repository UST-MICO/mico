package io.github.ustmico.micocore;

import org.springframework.data.repository.CrudRepository;

public interface ServiceRepository extends CrudRepository<Service, Long> {

        Service findByName(String name);
}
