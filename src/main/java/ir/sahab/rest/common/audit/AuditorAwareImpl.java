package ir.sahab.rest.common.audit;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The {@link AuditingEntityListener} uses this registered {@link AuditorAware} component to find user name to fill the
 * fields annotated by @CreatedBy and @LastModifiedBy.
 */
public class AuditorAwareImpl implements AuditorAware<String> {

    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }
}