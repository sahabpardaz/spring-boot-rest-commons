package ir.sahab.rest.common.apierror;

import java.util.Optional;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.common.MediaTypes;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

/**
 * This class handles exceptions thrown from all controller classes of the application. You can think of it as an
 * interceptor of exceptions thrown by methods annotated with {@link RequestMapping RequestMapping} or one of the
 * shortcuts ({@link GetMapping GetMapping}, {@link PostMapping PostMapping}, {@link PutMapping PutMapping}, {@link
 * DeleteMapping DeleteMapping}, {@link PatchMapping PatchMapping} ). Handlers for most type of general exceptions are
 * implemented in {@link ProblemHandling} and {@link SecurityAdviceTrait}. Here we have added a custom handler for
 * {@link ApiException}.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ApiExceptionHandler implements ProblemHandling, SecurityAdviceTrait {

    @ExceptionHandler({ApiException.class})
    public ResponseEntity<Problem> handleApiException(final ApiException ex, final NativeWebRequest request) {
        ThrowableProblem problem = Problem.builder()
                .with(ApiProblemField.TRACKING_ID.name().toLowerCase(), ex.getTrackingId())
                .with(ApiProblemField.EN_MESSAGE.name().toLowerCase(),
                        String.format(ex.getApiErrorCode().getEnMessage(), ex.getParameters()))
                .with(ApiProblemField.FA_MESSAGE.name().toLowerCase(),
                        String.format(ex.getApiErrorCode().getFaMessage(), ex.getParameters()))
                .with(ApiProblemField.ERROR_CODE.name().toLowerCase(), ex.getApiErrorCode().getName())
                .withDetail(String.valueOf(ex.getExtraData()))
                .withStatus(ex.getApiErrorCode().getHttpStatusCode())
                .withTitle(ex.getApiErrorCode().getHttpStatusCode().getReasonPhrase())
                .build();
        return create(ex, problem, request);
    }

    /**
     * Excludes exception's cause in returned problem.
     */
    @Override
    public boolean isCausalChainsEnabled() {
        return false;
    }

    /**
     * Changes returned problem's encoding to JSON UTF-8.
     */
    @Override
    public Optional<MediaType> negotiate(final NativeWebRequest request) {
        final Optional<MediaType> mediaType = ProblemHandling.super.negotiate(request);
        return mediaType
                .filter(MediaTypes.PROBLEM::equals)
                .map(type -> Optional.of(MediaType.APPLICATION_PROBLEM_JSON_UTF8))
                .orElse(mediaType);
    }

    @Override
    public ResponseEntity<Problem> process(ResponseEntity<Problem> entity, NativeWebRequest request) {
        return createResponseProblem((ServletWebRequest) request, entity);
    }

    /**
     * Adds following details to returned problem for all exceptions.
     * <ul>
     *     <li>User</li>
     *     <li>Api path</li>
     *     <li>Api HttpMethod</li>
     *     <li>Tracking id</li>
     *     <li>Timestamp</li>
     *     <li>English message</li>
     *     <li>Persian message</li>
     * </ul>
     */
    private ResponseEntity<Problem> createResponseProblem(ServletWebRequest request, ResponseEntity<Problem> entity) {
        ThrowableProblem problem = (ThrowableProblem) entity.getBody();

        // Add the fields from original problem.
        ProblemBuilder problemBuilder = Problem.builder()
                .withType(problem.getType())
                .withTitle(problem.getTitle())
                .withStatus(problem.getStatus())
                .withDetail(problem.getDetail())
                .withCause(problem.getCause());
        problem.getParameters().forEach(problemBuilder::with);

        // Add the extra fields that we can define their exact values
        problemBuilder.with(ApiProblemField.USER.name().toLowerCase(), getCurrentUser())
                .with(ApiProblemField.API_PATH.name().toLowerCase(), request.getRequest().getRequestURI())
                .with(ApiProblemField.HTTP_METHOD.name().toLowerCase(), request.getRequest().getMethod())
                .with(ApiProblemField.TIMESTAMP.name().toLowerCase(), System.currentTimeMillis());

        // Add the extra fields that we want to set their default values if they are not already set in the original
        // problem.
        if (!problem.getParameters().containsKey(ApiProblemField.TRACKING_ID.name().toLowerCase())) {
            problemBuilder.with(ApiProblemField.TRACKING_ID.name().toLowerCase(), UUID.randomUUID().toString());
        }
        if (!problem.getParameters().containsKey(ApiProblemField.EN_MESSAGE.name().toLowerCase())) {
            problemBuilder.with(ApiProblemField.EN_MESSAGE.name().toLowerCase(), problem.getMessage());
        }
        if (!problem.getParameters().containsKey(ApiProblemField.FA_MESSAGE.name().toLowerCase())) {
            problemBuilder.with(ApiProblemField.FA_MESSAGE.name().toLowerCase(), "درخواست مربوطه با خطا مواجه شد.");
        }

        return new ResponseEntity<>(problemBuilder.build(), entity.getHeaders(), entity.getStatusCode());
    }

    private String getCurrentUser() {
        String currentUser = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            currentUser = authentication.getName();
        }
        return currentUser;
    }
}
