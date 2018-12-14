package io.github.ust.mico.core.build;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableBuild extends CustomResourceDoneable<Build> {
    public DoneableBuild(Build resource, Function function) {
        super(resource, function);
    }
}
