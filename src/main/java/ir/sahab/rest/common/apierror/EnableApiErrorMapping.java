package ir.sahab.rest.common.apierror;

import ir.sahab.rest.common.apierror.EnableApiErrorMapping.ApiErrorMappingImporter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

/**
 * Annotation to enable API error mapping in application via annotation configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ApiErrorMappingImporter.class)
public @interface EnableApiErrorMapping {

    /**
     * It selects {@link ApiErrorMappingConfigurer} configuration to apply.
     */
    class ApiErrorMappingImporter implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata classMetadata) {
            return new String[]{ApiErrorMappingConfigurer.class.getName()};
        }
    }

    class ApiErrorMappingConfigurer {

        /**
         * Registers Jackson module to disable returning exception's stacktrace in returned problem. For more
         * information about this please read: https://github.com/zalando/problem#stack-traces-and-causal-chains
         */
        @Bean
        public ProblemModule problemModule() {
            return new ProblemModule().withStackTraces(false);
        }

        /**
         * Registers Jackson module which is a companion to {@link ProblemModule} to disable Jackson auto detection for
         * {@link ConstraintViolationProblem}. For more information about this please read:
         * https://opensource.zalando.com/problem/constraint-violation/
         */
        @Bean
        public ConstraintViolationProblemModule constraintViolationProblemModule() {
            return new ConstraintViolationProblemModule();
        }

        @Bean
        public ApiExceptionHandler globalApiExceptionHandler() {
            return new ApiExceptionHandler();
        }
    }
}
