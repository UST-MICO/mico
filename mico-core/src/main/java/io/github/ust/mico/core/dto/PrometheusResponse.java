package io.github.ust.mico.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PrometheusResponse {
    public static final String PROMETHEUS_SUCCESSFUL_RESPONSE = "success";
    private String status;
    private int value;

    public boolean wasSuccessful(){
        return status.equals(PROMETHEUS_SUCCESSFUL_RESPONSE);
    }

    @JsonProperty("data")
    @SuppressWarnings("unused")
    private void unpackNested(Map<String, Object> data) {
        //TODO find a better mapping solution
        List<Object> resultList = (List<Object>) data.get("result");
        Map<String,Object> resultEntry = (Map<String,Object>) resultList.get(0);
        List<String> dataPoint = (List<String>) resultEntry.get("value");
        this.value = Integer.parseInt(dataPoint.get(1));
    }

}
