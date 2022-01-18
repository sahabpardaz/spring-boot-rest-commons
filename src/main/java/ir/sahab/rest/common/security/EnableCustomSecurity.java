package ir.sahab.rest.common.security;

import ir.sahab.rest.common.security.Authenticator.BasicAuthenticator;
import ir.sahab.rest.common.security.EnableCustomSecurity.CustomSecurityImporter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Annotation to enable Spring Boot security in application via annotation configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(CustomSecurityImporter.class)
public @interface EnableCustomSecurity {

    String applicationBasePathPattern() default "/**";

    /**
     * It overwrites the default Spring granted authority prefix (ROLE_).
     */
    String authorityPrefix() default "";

    String[] ignoredPaths() default {};

    /**
     * It selects {@link SecurityConfigurer} configuration to apply.
     */
    class CustomSecurityImporter implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata classMetadata) {
            CustomSecurityMetadata customSecurityMetadata = CustomSecurityMetadata.getInstance();
            customSecurityMetadata.fillFrom(classMetadata);
            return new String[] {SecurityConfigurer.class.getName()};
        }
    }
}