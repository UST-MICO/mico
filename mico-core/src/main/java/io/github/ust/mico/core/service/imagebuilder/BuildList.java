package io.github.ust.mico.core.service.imagebuilder;

import io.fabric8.kubernetes.client.CustomResourceList;
import io.github.ust.mico.core.service.imagebuilder.buildtypes.Build;

public class BuildList extends CustomResourceList<Build> {

    private static final long serialVersionUID = 3524519220752406464L;

}
