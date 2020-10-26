package ir.sahab.rest.common.requestlog;

import static ir.sahab.rest.common.requestlog.CustomRequestLoggingPropertiesPostProcessor.overridePrimaryLogbookProperties;
import static ir.sahab.rest.common.requestlog.CustomRequestLoggingPropertiesPostProcessor.setLogBookEnable;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.sahab.rest.common.requestlog.EnableCustomRequestLogging.CustomRequestLoggingImporter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration;

/**
 * Annotation to enable request/response logging in application via annotation configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({CustomRequestLoggingImporter.class})
public @interface EnableCustomRequestLogging {

    LogLevel logLevel() default LogLevel.INFO;

    /* Comma separated list of header names that need obfuscation */
    String obfuscateHeaders() default "Authorization,X-API-KEY,X-Auth-Token";

    /* Comma separated list of parameter names that need obfuscation */
    String obfuscateParameters() default "X-API-KEY";

    /* Indicates the max request/response body size to truncates the body and appends .... */
    int maxBodySize() default 1000;

    /**
     * It selects {@link LogConfigurer} configuration to apply. Since import configuration executes before any
     * auto-configuration process (especially {@link LogbookAutoConfiguration}), here we can customize the Logbook
     * behavior. Also we provide some default values for some Logbook properties here.
     */
    class CustomRequestLoggingImporter implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata classMetadata) {
            CustomRequestLoggingMetadata customRequestLoggingMetadata = CustomRequestLoggingMetadata.getInstance();
            customRequestLoggingMetadata.fillFrom(classMetadata);
            setLogBookEnable(true);
            overridePrimaryLogbookProperties(customRequestLoggingMetadata);
            return new String[] {LogConfigurer.class.getName()};
        }

    }

    class LogConfigurer {

        @Bean
        public HttpRequestLogWriter httpRequestLogWriter() {
            return new HttpRequestLogWriter();
        }

        @Bean
        public HttpLogFormatter httpLogFormatter(final ObjectMapper mapper) {
            return new JsonHttpLogFormatter(mapper);
        }
    }
}
