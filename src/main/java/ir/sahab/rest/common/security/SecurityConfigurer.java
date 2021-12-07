package ir.sahab.rest.common.security;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import ir.sahab.rest.common.apierror.ApiExceptionHandler;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

/**
 * It configures SpringBoot security by suitable configuration required by a typical application that provides REST
 * APIs.
 */
@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
@Import(SecurityProblemSupport.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProblemSupport problemSupport;

    public SecurityConfigurer() {
        // Spring support for sending asynchronous processing of the requests with propagated SecurityContext.
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private final CustomSecurityMetadata customSecurityMetadata = CustomSecurityMetadata.getInstance();

    // The application-wide matcher with the specific pattern which will match all HTTP methods in a case insensitive
    // manner. All internal rest services which needs to be authenticated must be start with this pattern.
    private final RequestMatcher protectedPathMatcher = new AntPathRequestMatcher(
            customSecurityMetadata.getApplicationBasePathPattern());

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // If ApiErrorMapping is enabled, we want to use that library for mapping authentication/authorization errors to
        // HTML responses. Otherwise we are going to response with a more simple response containing the UNAUTHORIZED
        // status code.
        AccessDeniedHandler accessDeniedHandler =
                isApiErrorMappingEnabled() ? problemSupport : new AccessDeniedHandlerImpl();

        http.sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)

                .and()
                // It is sufficient to successfully extract the authentication object by the authentication filter.
                .authenticationProvider(noMoreAuthenticationProvider())
                .addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest()
                .authenticated()

                // This config is designed for RESTs. No UI pages is involved.
                .and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable();
    }

    /**
     * Registers public paths which we need to bypass security checks for them.
     */
    @Override
    public void configure(WebSecurity web) {
        final String[] ignoredPath = customSecurityMetadata.getIgnoredPaths();
        if (null != ignoredPath) {
            web.ignoring().antMatchers(ignoredPath);
        }
        web.ignoring().antMatchers("/actuator/**");
    }

    /**
     * Returns authentication filter that authenticates requests in protected paths. It calls the {@link Authenticator}
     * object provided as argument to {@link EnableCustomSecurity}.
     */
    @Bean
    AuthenticationFilter authenticationFilter() throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(
                getApplicationContext(),
                customSecurityMetadata.getAuthenticatorClass(),
                protectedPathMatcher);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler());
        AuthenticationEntryPoint authenticationEntryPoint =
                isApiErrorMappingEnabled() ? problemSupport : new HttpStatusEntryPoint(UNAUTHORIZED);
        filter.setAuthenticationFailureHandler(authenticationEntryPoint::commence);
        return filter;
    }

    /**
     * Disables redirecting after successful login. Redirection is not required with pure REST.
     */
    @Bean
    SimpleUrlAuthenticationSuccessHandler successHandler() {
        final SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setRedirectStrategy((request, response, url) -> {
        });
        return successHandler;
    }

    /**
     * Disables Spring boot automatic filter registration for {@link AuthenticationFilter}, because we want to manually
     * register this filter in Spring security configuration.
     */
    @Bean
    FilterRegistrationBean<AuthenticationFilter> disableAutoRegistration(
            final AuthenticationFilter filter) {
        final FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Overwrites the default Spring granted authority prefix (ROLE_) with the one defined in {@link
     * CustomSecurityMetadata#getAuthorityPrefix()}.
     */
    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(customSecurityMetadata.getAuthorityPrefix());
    }

    /**
     * Since Spring security {@link AuthenticationManager} needs at least one instance of {@link AuthenticationProvider}
     * ,so we implement this provider which has not any logic and it simply accepts all extracted authentications. In
     * fact we extract authentication objects by authentication filter and the authentication filter calls the {@link
     * Authenticator} object which is provided as an argument to {@link EnableCustomSecurity}. Successful extraction is
     * sufficient that we conclude the request is authenticated and the user information and its grants is set in the
     * authentication object.
     *
     * @see Authenticator
     */
    @Bean
    AuthenticationProvider noMoreAuthenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return authentication;
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return true;
            }
        };
    }

    private boolean isApiErrorMappingEnabled() {
        try {
            getApplicationContext().getBean(ApiExceptionHandler.class);
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
        return true;
    }
}
