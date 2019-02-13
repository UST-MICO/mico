package io.github.ust.mico.core.service.imagebuilder;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;
import io.github.ust.mico.core.service.imagebuilder.buildtypes.Build;

@SuppressWarnings("SpellCheckingInspection")
public class DoneableBuild extends CustomResourceDoneable<Build> {
    public DoneableBuild(Build resource, Function function) {
        super(resource, function);
    }
}
