package ir.sahab.rest.common.audit;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Data holder class for metadata of {@link EnableCustomAuditing} annotation.
 */
public final class CustomAuditingMetadata {

    private static final CustomAuditingMetadata instance = new CustomAuditingMetadata();

    private String schema;

    public static CustomAuditingMetadata getInstance() {
        return instance;
    }

    public void filFrom(AnnotationMetadata classMetadata) {
        MultiValueMap<String, Object> allAnnotationAttributes = classMetadata
                .getAllAnnotationAttributes(EnableCustomAuditing.class.getName());
        schema = (String) allAnnotationAttributes.getFirst("schema");
        if (StringUtils.isEmpty(schema)) {
            throw new IllegalArgumentException("You must provide schema for revision entity table!");
        }
    }

    public String getSchema() {
        return this.schema;
    }
}
