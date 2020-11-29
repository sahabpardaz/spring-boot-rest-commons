package ir.sahab.rest.common.security;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Data holder class for metadata of {@link EnableCustomSecurity} annotation.
 */
public class CustomSecurityMetadata {

    private static final CustomSecurityMetadata instance = new CustomSecurityMetadata();

    private Class<? extends Authenticator> authenticatorClass;
    private String applicationBasePathPattern;
    private String[] ignoredPaths;
    private String authorityPrefix;

    public static CustomSecurityMetadata getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public void fillFrom(AnnotationMetadata securityAttributes) {
        MultiValueMap<String, Object> allAnnotationAttributes = securityAttributes
                .getAllAnnotationAttributes(EnableCustomSecurity.class.getName());
        authenticatorClass = (Class<? extends Authenticator>) allAnnotationAttributes.getFirst("authenticator");
        ignoredPaths = (String[]) allAnnotationAttributes.getFirst("ignoredPaths");
        authorityPrefix = (String) allAnnotationAttributes.getFirst("authorityPrefix");
        applicationBasePathPattern = (String) allAnnotationAttributes.getFirst("applicationBasePathPattern");
        if (StringUtils.isEmpty(applicationBasePathPattern)) {
            throw new IllegalArgumentException(
                    "You must provide application base path pattern!, eg. /backend-api/** ");
        }
    }

    public Class<? extends Authenticator> getAuthenticatorClass() {
        return authenticatorClass;
    }

    public String getApplicationBasePathPattern() {
        return this.applicationBasePathPattern;
    }

    public String[] getIgnoredPaths() {
        return ignoredPaths;
    }

    public String getAuthorityPrefix() {
        return authorityPrefix;
    }
}