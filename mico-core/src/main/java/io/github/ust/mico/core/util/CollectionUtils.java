package io.github.ust.mico.core.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionUtils {
    
    @SafeVarargs
    public <T> List<T> listOf(T... items) {
        return Arrays.asList(items);
    }
    
    public <K, V> Map<K, V> mapOf(K key, V value) {
        Map<K, V> map = new HashMap<K, V>();
        map.put(key, value);
        return map;
        
    }
    
    @NoArgsConstructor
    public class MapBuilder<K, V> {
        
        private Map<K, V> map = new HashMap<K, V>();
        
        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }
        
        public Map<K, V> build() {
            return map;
        }
        
    }
    

}
