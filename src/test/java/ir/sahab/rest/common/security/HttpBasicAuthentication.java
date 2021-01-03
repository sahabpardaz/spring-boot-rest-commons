package ir.sahab.rest.common.security;

import java.util.Base64;

/**
 * This class helps making HTTP basic authentication header.
 */
public class HttpBasicAuthentication {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Returns the HTTP basic authentication header value for the given username and password. This output string is
     * calculated as below:
     * <code>"Basic " + Base64.encode(username + ":" + password)</code>
     */
    public static String of(String user, String pass) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    }

}