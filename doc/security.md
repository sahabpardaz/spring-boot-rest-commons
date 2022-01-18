# Custom Security

## The Problem it Solves

This library does a custom security configuration suitable for a typical REST API in SpringBoot applications.
Spring is very flexible regarding security. There are many classes, you can extend, implement and configure to customize
the SpringBoot security. ‌But what this library provides is an easy and fast configuration; a typical configuration that
is suitable for most RESTful APIs. These are the features of these custom configuration:

- UI-related aspects like login page, logout page, and the security error pages are disabled.
- Session-related aspects are disabled. This library has targeted a RESTful API with no sense of the user sessions. It
assumes that the user security data is attached to all requests. Maybe it is filled either directly or by an external
system that does the session management. However, it is hidden from the RESTful server.
- For security errors, the client will get an HTTP response packet with proper status codes like 401 and 403 out of the
box.
- This library provides an easy way to implement your custom authentication/authorization mechanism. The simplest
mechanism is [Basic HTTP authentication](https://en.wikipedia.org/wiki/Basic_access_authentication). This library
provides it by default. So you do not need to implement anything in this case. The user is extracted from the
authorization header in requests, and you can access it from Spring `SecurityContext`. You can handle more complicated
cases too. For example to recognize the user and find its roles/permissions, you may want to read a HTTP header
containing a [JWT token](https://jwt.io/) prepared by an
[OAuth server](https://www.ory.sh/run-oauth2-server-open-source-api-security/).
In such cases, you should just implement an `Authenticator` callback and register it as a Spring bean. The `Authenticator`
gets the HTTP request as input and returns the corresponding `Authentication` object (representing a user and its 
roles/permissions) or throws the `AuthenticationException` if the HTTP request does not contain valid authentication data.
- This library provides an easy-to-use annotation for SpringBoot to configure all of these at once:
`@EnableCustomSecurity`

## Sample Usage

Here we demonstrate two cases. One for a REST API with [Basic HTTP authentication] mechanism and one for a more
complicated authentication pattern.

### Basic HTTP Authentication

Introduce and enable this library in your SpringBoot application by adding the `@EnableCustomSecurity` annotation:

```java
@SpringBootApplication
@EnableCustomSecurity()
public class Application {
   ...
}

```

Because we have not set an authenticator callback, the default (basic HTTP authentication) is enabled. Now when
processing a request, we can read the `SecurityContext` to see the user/pass that is automatically extracted from the
authorization header.

```java
@PostMapping()
public ResponseEntity<?> postRequest(HttpServletRequest request) {

     String userName = SecurityContextHolder.getContext().getAuthentication().getName();
     ...
}
```

### Custom Authentication

Again we are going to enable our custom security by adding the `@EnableCustomSecurity` annotation, but this time we
register a bean as our custom `Authenticator` implementation:

```java
@SpringBootApplication
@EnableCustomSecurity(applicationBasePathPattern= "/protected-api/**", ignoredPaths = {"/ignored-auth/**"})
public class Application {
   @Bean
   public Authenticator authenticator() {
       return new CustomAuthenticator();
   }
   ...
}

```

As you see there are two arguments passed to the @EnableCustomSecurity:

- applicationBasePathPattern: specifies the base path pattern under which security (authentication/authorization) is
enabled. By default, we will enable security on all paths: "/**"
- ignoredPaths: excludes some paths to be ignored from base path. By default, no path is excluded. 

Besides these parameters, you can also pass another argument `authorityPrefix`. By default, Spring adds the prefix
"ROLE_" to the role names returned by the authentication object. But we do not like to manipulate the original role
names, so we do not append anythings to those names. Using this parameter, you can define your own prefix of choice.
At the next step, we are going to implement the `CustomAuthenticator` class:

```java
public class CustomAuthenticator implements Authenticator {

    private final ObjectMapper objectMapper;

    public CustomAuthenticator() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Authentication authenticate(HttpServletRequest request) throws AuthenticationException {
        final String authorizationHeader = ofNullable(request.getHeader("X-Authorization")).orElse(null);
        if (StringUtils.isBlank(authorizationHeader)) {
            throw new BadCredentialsException("No user token is provided!");
        }
        try {
            return objectMapper.readValue(authorizationHeader.trim(), UserToken.class);
        } catch (IOException e) {
            throw new BadCredentialsException("Failed to parse the Portal user!", e);
        }
    }
}

```

As you see, the above authenticator, assumes that there is a JSON token provided in a header named X-Authorization. JSON
deserializer knows how to deserialize the token from the definition of class `UserToken` that is an implementation of
`Authentication`; an implementation specific to our REST service:

```java
@JsonIgnoreProperties({"authorities"})
public class UserToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private Set<String> permissions = new HashSet<>();
    private Set<String> roles = new HashSet<>();
    private String username;
    private boolean enabled;
    private String remoteIp;
    private String firstName;
    private String lastName;

    public UserToken() {
        super(new HashSet<>());
    }

    public PortalUser(String userName, Set<String> permissions) {
        super(Collections.emptySet());
        this.username = userName;
        this.permissions = permissions;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public Object getCredentials() {
        return username;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new AssertionError("Unexpected exception while serializing a UserToken to JSON.", ex);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserToken.class.getSimpleName() + "[", "]")
                .add("permissions=" + permissions)
                .add("roles=" + roles)
                .add("username='" + username + "'")
                .add("enabled=" + enabled)
                .add("remoteIp='" + remoteIp + "'")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .toString();
    }
}
```

You may think it is not secure the client tells its permissions, but in fact, you will see this design where the REST
is not called directly from the client but is proxied by a certified gateway that is responsible for providing the token.
However, there are other approaches we can think of. For example in implementation of `Authenticator` it is possible to
fill the roles/permissions after an inquiry from an external system like LDAP.