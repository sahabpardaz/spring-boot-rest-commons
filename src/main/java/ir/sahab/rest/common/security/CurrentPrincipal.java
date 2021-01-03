package ir.sahab.rest.common.security;

import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A utility class that simplifies getting name or checking authorities of current authenticated principal.
 */
public final class CurrentPrincipal {

    private CurrentPrincipal() {
    }

    public static String getName() {
        return null != SecurityContextHolder.getContext().getAuthentication()
                ? SecurityContextHolder.getContext().getAuthentication().getName() : "";
    }

    public static boolean hasAuthority(String selectedAuthority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            @SuppressWarnings("unchecked")
            Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(selectedAuthority)) {
                    return true;
                }
            }
        }
        return false;
    }
}