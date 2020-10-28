package ir.sahab.rest.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * The interface that provides method for extracting  an {@link Authentication} object from a HTTP request. If the
 * method returns successfully with an {@link Authentication} object, it means the request is authenticated and the user
 * information and its grants is successfully set in the returned object.
 */
public interface Authenticator {

    Authentication authenticate(HttpServletRequest request) throws AuthenticationException;

    /**
     * Basic authenticator which just extracts credential information from a basic authentication header.
     */
    class BasicAuthenticator implements Authenticator {

        @Override
        public Authentication authenticate(HttpServletRequest request) {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Basic")) {
                String base64Credentials = authorizationHeader.substring("Basic ".length()).trim();
                String decodedHeader = new String(Base64.getDecoder().decode(base64Credentials),
                        StandardCharsets.UTF_8);
                String[] credentials = decodedHeader.split(":", 2);
                return new UsernamePasswordAuthenticationToken(credentials[0], credentials[1], Collections.emptySet());
            }
            throw new BadCredentialsException("Invalid Credentials!");
        }
    }
}
