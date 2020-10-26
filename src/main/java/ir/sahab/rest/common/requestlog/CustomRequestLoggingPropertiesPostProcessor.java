package ir.sahab.rest.common.requestlog;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

/**
 * Customizes the application's {@link Environment} prior to the application context being refreshed. We have two
 * purposes here:
 * <ol>
 * <li> We want to disable the logbook at application startup and then enable it just if it is explicitly requested by
 * {@link EnableCustomRequestLogging} annotation.
 * <li> We want to set some primary logbook properties. In fact we want to set these behaviors: obfuscating headers that
 * contain credential values and cutting the request/response body in logs if they are too lengthy.
 * </ol>
 */
@Order
public class CustomRequestLoggingPropertiesPostProcessor implements EnvironmentPostProcessor {

    private static final String LOGBOOK_PROPERTY_SOURCE_NAME = "logbookOverrideProps";

    private static ConfigurableEnvironment environment;


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        CustomRequestLoggingPropertiesPostProcessor.environment = environment;
        setLogBookEnable(false);
    }

    public static void setLogBookEnable(boolean enable) {
        Map<String, Object> map = new HashMap<>();
        map.put("logbook.filter.enabled", enable);
        map.put("logbook.secure-filter.enabled", enable);
        addOrReplace(environment.getPropertySources(), map);
    }

    public static void overridePrimaryLogbookProperties(CustomRequestLoggingMetadata requestLoggingMetadata) {
        Map<String, Object> map = new HashMap<>();
        map.put("logbook.write.max-body-size", requestLoggingMetadata.getMaxBodySize());
        setListPropertyValue(map, "logbook.obfuscate.headers", requestLoggingMetadata.getObfuscateHeaders());
        setListPropertyValue(map, "logbook.obfuscate.parameters", requestLoggingMetadata.getObfuscateParameters());
        addOrReplace(environment.getPropertySources(), map);
    }

    private static void setListPropertyValue(Map<String, Object> map, String key, String value) {
        String propertyValue = environment.getProperty(key);
        if (StringUtils.isEmpty(propertyValue)) {
            map.put(key, value);
        } else {
            map.put(key, value + "," + propertyValue);
        }
    }

    private static void addOrReplace(MutablePropertySources sources, Map<String, Object> map) {
        if (sources.contains(LOGBOOK_PROPERTY_SOURCE_NAME)) {
            MapPropertySource source = (MapPropertySource) sources.get(LOGBOOK_PROPERTY_SOURCE_NAME);
            for (String key : map.keySet()) {
                source.getSource().put(key, map.get(key));
            }
        } else {
            // Add our property source as first property provider
            sources.addFirst(new MapPropertySource(LOGBOOK_PROPERTY_SOURCE_NAME, map));
        }
    }
}
