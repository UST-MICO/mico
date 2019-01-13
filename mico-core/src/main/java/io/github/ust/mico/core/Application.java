package io.github.ust.mico.core;

public class Application extends Service {
    private String deployInformation;
    private String visualizationData;

    public Application() {
    }

    public Application(String shortName) {
        super(shortName);
    }

    public Application(String shortName, String version) {
        super(shortName, version);
    }

    public Application(String shortName, String version, String description) {
        super(shortName, version, description);
    }

    //TODO: Verify if all are necessary
    public String getDeployInformation() {
        return deployInformation;
    }

    public void setDeployInformation(String deployInformation) {
        this.deployInformation = deployInformation;
    }

    public String getVisualizationData() {
        return visualizationData;
    }

    public void setVisualizationData(String visualizationData) {
        this.visualizationData = visualizationData;
    }
}
