package org.demacia.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.Map;

/**
 * @author hepenglin
 * @since 2024/11/30 22:10
 */
public class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        String jsonStr = null;
        try {
            jsonStr = OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    public static Map<String, Object> toMap(Object object) {
        TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
        MapType mapType = typeFactory.constructMapType(Map.class, String.class, Object.class);
        return OBJECT_MAPPER.convertValue(object, mapType);
    }
}
