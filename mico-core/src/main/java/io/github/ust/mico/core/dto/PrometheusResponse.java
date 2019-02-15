package io.github.ust.mico.core.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
// TODO: Class comment.
public class PrometheusResponse {
    
    // TODO: Add comments for fields.
    
    public static final String PROMETHEUS_SUCCESSFUL_RESPONSE = "success";
    
    private String status;
    private int value;

    public boolean wasSuccessful(){
        return status.equals(PROMETHEUS_SUCCESSFUL_RESPONSE);
    }

    // TODO: Optimize
    @JsonProperty("data")
    @SuppressWarnings("unchecked")
    private void unpackNested(Map<String, Object> data) {
        //TODO find a better mapping solution
        List<Object> resultList = (List<Object>) data.get("result");
        Map<String,Object> resultEntry = (Map<String,Object>) resultList.get(0);
        List<String> dataPoint = (List<String>) resultEntry.get("value");
        this.value = Integer.parseInt(dataPoint.get(1));
    }

}
