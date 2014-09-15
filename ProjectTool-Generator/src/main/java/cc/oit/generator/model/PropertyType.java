package cc.oit.generator.model;

import java.util.HashMap;

/**
 * Created by Chanedi
 */
public class PropertyType extends HashMap {

    public PropertyType(String javaType) {
        super();
        addType("java", javaType);
    }

    public void addType(String typeKey, String type) {
        put(typeKey, type);
    }

}
