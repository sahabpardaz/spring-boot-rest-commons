package ir.sahab.rest.common.requestlog;

import org.springframework.boot.logging.LogLevel;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;

/**
 * Data holder class for metadata of {@link EnableCustomRequestLogging} annotation.
 */
public final class CustomRequestLoggingMetadata {

    private static final CustomRequestLoggingMetadata instance = new CustomRequestLoggingMetadata();

    private LogLevel logLevel;
    private String obfuscateHeaders;
    private String obfuscateParameters;
    private Integer maxBodySize;

    public static CustomRequestLoggingMetadata getInstance() {
        return instance;
    }

    public void fillFrom(AnnotationMetadata loggingConfigurationAttributes) {
        MultiValueMap<String, Object> loggingAnnotationAttributes = loggingConfigurationAttributes
                .getAllAnnotationAttributes(EnableCustomRequestLogging.class.getName());
        logLevel = (LogLevel) checkNotNull(loggingAnnotationAttributes.getFirst("logLevel"), "logLevel");
        obfuscateHeaders = (String) checkNotNull(loggingAnnotationAttributes.getFirst("obfuscateHeaders"),
                "obfuscateHeaders");
        obfuscateParameters = (String) checkNotNull(loggingAnnotationAttributes.getFirst("obfuscateParameters"),
                "obfuscateParameters");
        maxBodySize = (Integer) checkNotNull(loggingAnnotationAttributes.getFirst("maxBodySize"), "maxBodySize");
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getObfuscateHeaders() {
        return obfuscateHeaders;
    }

    public String getObfuscateParameters() {
        return obfuscateParameters;
    }

    public Integer getMaxBodySize() {
        return maxBodySize;
    }

    private Object checkNotNull(Object object, String inputName) {
        if (object == null) {
            throw new IllegalArgumentException(inputName + " must not be empty!");
        }
        return object;
    }
}