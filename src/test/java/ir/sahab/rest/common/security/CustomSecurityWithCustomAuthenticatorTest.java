package ir.sahab.rest.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.collect.ImmutableSet;
import ir.sahab.rest.common.security.CustomSecurityWithCustomAuthenticatorTest.RestServiceSetup;
import ir.sahab.rest.common.testapp.TestOrderController;
import java.util.Collection;
import java.util.HashSet;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestServiceSetup.class})
@AutoConfigureMockMvc
public class CustomSecurityWithCustomAuthenticatorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCustomAuthenticator() throws Exception {

        // Call the service without providing the user token
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/protected-api")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Call the service with a user that is not authenticated
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/protected-api")
                .accept(MediaType.APPLICATION_JSON)
                .header("username", "hamid"))
                .andExpect(status().isUnauthorized());

        // Call the service (that does not require a permission) with an authenticated user
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/protected-api")
                .accept(MediaType.APPLICATION_JSON)
                .header("username", "ali"))
                .andExpect(status().isOk());

        // Call the service with a user that has the required permission
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/protected-with-permission")
                .accept(MediaType.APPLICATION_JSON)
                .header("username", "saeed"))
                .andExpect(status().isOk());

        // Call the service with a user that has not the required permission
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/protected-with-permission")
                .accept(MediaType.APPLICATION_JSON)
                .header("username", "ali"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testIgnoredPath() throws Exception {
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/ignored-path")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @SpringBootApplication(scanBasePackages = "ir.sahab.rest.common.testapp")
    @PropertySource("classpath:rest-commons-test.properties")
    @EnableJpaRepositories(basePackages = "ir.sahab.rest.common.testapp")
    @EntityScan(basePackages = "ir.sahab.rest.common.testapp")
    @EnableCustomSecurity(
            applicationBasePathPattern = "/api/**",
            ignoredPaths = TestOrderController.REST_BASE_PATH + "/ignored-path",
            authorityPrefix = "orders.")
    public static class RestServiceSetup {

        @Bean
        public Authenticator authenticator() {
            return new TestAuthenticator();
        }
    }

    public static class TestAuthenticator implements Authenticator {

        @Override
        public Authentication authenticate(HttpServletRequest request) throws AuthenticationException {
            String username = request.getHeader("username");
            Collection<? extends GrantedAuthority> authorities = null;
            // User with special permission
            if ("saeed".equalsIgnoreCase(username)) {
                authorities = ImmutableSet.of(new SimpleGrantedAuthority("orders.sample.permission"));
            }
            // User without any special permission
            if ("ali".equalsIgnoreCase(username)) {
                authorities = new HashSet<>();
            }
            if (authorities != null) {
                return new UsernamePasswordAuthenticationToken(username, "password", authorities);
            }
            throw new BadCredentialsException("Invalid credential!");
        }
    }
}