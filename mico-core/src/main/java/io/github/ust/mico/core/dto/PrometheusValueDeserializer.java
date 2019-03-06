package io.github.ust.mico.core.dto;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class PrometheusValueDeserializer extends StdDeserializer<Map> {

    protected PrometheusValueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Map deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Map<String, Integer> resultData;
        ArrayNode resultArray = p.getCodec().readTree(p);
        System.out.println(resultArray.size());
        for (JsonNode jsonNode : resultArray) {
            JsonNode metricNode = jsonNode.get("metric");
        }
        resultArray.get("value").decimalValue();
        return null;
    }
}
