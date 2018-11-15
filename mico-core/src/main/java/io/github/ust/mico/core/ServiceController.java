package io.github.ust.mico.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;
    
    @GetMapping("/service")
    public List<Service> getAllServices(){
        return (List<Service>) serviceRepository.findAll();
    }

    @GetMapping("/service/{id}")
    public Service getServiceById(@PathVariable Long id){
        return serviceRepository.findById(id).orElseThrow(() -> new io.github.ust.mico.core.ServiceNotFoundException(id));
    }

    @PostMapping(value = "/service")
    public Service createService(@RequestBody Service newService){
        return serviceRepository.save(newService);
    }

    @DeleteMapping("/service/{id}")
    public void deleteServiceById(@PathVariable Long id){
        serviceRepository.deleteById(id);
    }

    @PutMapping("/service/{id}")
    public Service replaceService(@RequestBody Service newService, @PathVariable Long id){
        return serviceRepository.findById(id).map(service -> {
            service.setName(newService.getName());
            service.setContact(newService.getContact());
            service.setDescription(newService.getDescription());
            service.setServiceDescriptions(newService.getServiceDescriptions());
            service.setVcsRoot(newService.getVcsRoot());
            service.setDependsOn(newService.getDependsOn());
            service.setDockerfile(newService.getDockerfile());
            service.setLifecycle(newService.getLifecycle());
            service.setOwner(newService.getOwner());
            service.setLinks(newService.getLinks());
            service.setPredecessor(newService.getPredecessor());
            service.setTags(newService.getTags());
            service.setShortName(newService.getShortName());
            service.setVersion(newService.getVersion());
            service.setType(newService.getType());
            return serviceRepository.save(service);
        }).orElseGet(() -> {
            return serviceRepository.save(newService);
        });
    }
}
