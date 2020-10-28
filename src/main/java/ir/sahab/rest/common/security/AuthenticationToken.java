package ir.sahab.rest.common.security;

import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Authentication Object based on {@link Authentication} object.
 */
public class AuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 0L;

    private final String principal;

    public AuthenticationToken(Authentication authentication) {
        super(authentication.getAuthorities());
        this.principal = (String) authentication.getPrincipal();
        super.setAuthenticated(this.principal != null);
        setDetails(authentication);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthenticationToken)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AuthenticationToken that = (AuthenticationToken) o;
        return Objects.equals(getPrincipal(), that.getPrincipal());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPrincipal());
    }
}
