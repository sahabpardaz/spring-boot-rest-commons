package ir.sahab.rest.common.apierror;

import static ir.sahab.rest.common.security.CustomSecurityWithDefaultAuthenticatorTest.TEST_VALID_USER_NAME;
import static ir.sahab.rest.common.testapp.TestOrderController.REST_BASE_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.sahab.rest.common.apierror.ApiErrorMappingWithSecurityTest.RestServiceSetup;
import ir.sahab.rest.common.security.Authenticator;
import ir.sahab.rest.common.security.EnableCustomSecurity;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestServiceSetup.class})
@AutoConfigureMockMvc
public class ApiErrorMappingWithSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSecurityExceptions() throws Exception {

        // User is unauthorized
        mockMvc.perform(get(REST_BASE_PATH + "/order/1")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "hamid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value("401"))
                .andExpect(jsonPath("$.detail").value("Invalid username/password!"))
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "/order/1"))
                .andExpect(jsonPath("$.http_method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message").value("Unauthorized: Invalid username/password!"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // Access is denied
        mockMvc.perform(get(REST_BASE_PATH + "/protected-with-permission")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "saeed"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value("403"))
                .andExpect(jsonPath("$.detail").value("Access is denied"))
                .andExpect(jsonPath("$.user").value(TEST_VALID_USER_NAME))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "/protected-with-permission"))
                .andExpect(jsonPath("$.http_method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message").value("Forbidden: Access is denied"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));
    }

    @SpringBootApplication(scanBasePackages = "ir.sahab.rest.common.testapp")
    @PropertySource("classpath:rest-commons-test.properties")
    @EnableJpaRepositories(basePackages = "ir.sahab.rest.common.testapp")
    @EntityScan(basePackages = {
            "ir.sahab.rest.common.testapp"
    })
    @EnableApiErrorMapping
    @EnableCustomSecurity(applicationBasePathPattern = "/api/**")
    public static class RestServiceSetup {

        @Bean
        public Authenticator authenticator() {
            return new TestAuthenticator();
        }
    }

    public static class TestAuthenticator implements Authenticator {

        @Override
        public Authentication authenticate(HttpServletRequest request) throws AuthenticationException {
            String authorization = request.getHeader("Authorization");
            if (StringUtils.isEmpty(authorization) || !TEST_VALID_USER_NAME.equalsIgnoreCase(authorization)) {
                throw new BadCredentialsException("Invalid username/password!");
            }
            return new UsernamePasswordAuthenticationToken(TEST_VALID_USER_NAME, "password");
        }
    }
}
