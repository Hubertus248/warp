package pl.warp.engine.graphics.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jaca777
 *         Created 2016-12-10 at 12
 */
public class PropertyMetadata {
    private String propertyClassName;
    private Map<String, Metadata> propertyValues;

    public PropertyMetadata(String propertyName) {
        this.propertyClassName = propertyName;
        this.propertyValues = new HashMap<>();
    }

    public String getPropertyClassName() {
        return propertyClassName;
    }

    public Map<String, Metadata> getPropertyValues() {
        return propertyValues;
    }
}
