package ir.sahab.rest.common.audit;

import ir.sahab.rest.common.audit.EnableCustomAuditing.CustomAuditingImporter;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Table;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Annotation to enable auditing in JPA via annotation configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(CustomAuditingImporter.class)
@EnableJpaAuditing
public @interface EnableCustomAuditing {

    String schema() default "";

    /**
     * It selects {@link AuditConfigurer} configuration to apply.
     */
    class CustomAuditingImporter implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata classMetadata) {
            CustomAuditingMetadata auditingMetadata = CustomAuditingMetadata.getInstance();
            auditingMetadata.filFrom(classMetadata);
            overrideSchemaOnRevisionTable(auditingMetadata.getSchema());
            return new String[]{AuditConfigurer.class.getName()};
        }

        private void overrideSchemaOnRevisionTable(String schema) {
            RevisionTableAnnotation revisionTableAnnotation = new RevisionTableAnnotation(schema);
            alterAnnotationValue(RevisionEntity.class, Table.class, revisionTableAnnotation);
        }

        @SuppressWarnings("unchecked")
        private void alterAnnotationValue(Class<?> targetClass, Class<? extends Annotation> targetAnnotation,
                Annotation targetValue) {
            try {
                Method method = Class.class.getDeclaredMethod("annotationData", (Class<?>[]) null);
                method.setAccessible(true);

                Object annotationData = method.invoke(targetClass);

                Field annotations = annotationData.getClass().getDeclaredField("annotations");
                annotations.setAccessible(true);

                Map<Class<? extends Annotation>, Annotation> map =
                        (Map<Class<? extends Annotation>, Annotation>) annotations.get(annotationData);
                map.put(targetAnnotation, targetValue);
            } catch (Exception e) {
                throw new AssertionError(
                        "Unable to alter target class:" + targetClass.getName() + " with annotation:" + targetValue);
            }
        }
    }

    @EntityScan(basePackages = "ir.sahab.rest.common.audit")
    class AuditConfigurer {

        @Bean
        public AuditorAwareImpl auditorAware() {
            return new AuditorAwareImpl();
        }

        @Bean
        AuditReader auditReader(ApplicationContext context) {
            EntityManagerFactory entityManagerFactory = context.getBean(EntityManagerFactory.class);
            return AuditReaderFactory.get(entityManagerFactory.createEntityManager());
        }
    }
}
