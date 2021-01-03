package ir.sahab.rest.common.security;

import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class with a collection of static methods which help to do security-related things like getting current user
 * and so on.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String getCurrentUser() {
        return null != SecurityContextHolder.getContext().getAuthentication()
                ? SecurityContextHolder.getContext().getAuthentication().getName() : "admin";
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