package io.github.ust.mico.core.util;

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import io.github.ust.mico.core.model.MicoServiceInterface;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UIDUtils {
    
    public final String uidFor(MicoApplication application) {
        return createUid(application.getShortName());
    }
    
    public final String uidFor(MicoService service) {
        return createUid(service.getShortName());
    }
    
    public final String uidFor(MicoServiceInterface serviceInterface) {
        return createUid(serviceInterface.getServiceInterfaceName());
    }
    
    private final String createUid(String prefix) {
        return prefix + "-" + RandomStringFactory.randomAlphanumeric();
    }

}
