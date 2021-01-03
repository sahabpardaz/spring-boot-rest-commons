package ir.sahab.rest.common.security;

import static ir.sahab.rest.common.security.HttpBasicAuthentication.AUTHORIZATION_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.sahab.rest.common.security.CustomSecurityWithDefaultAuthenticatorTest.RestServiceSetup;
import ir.sahab.rest.common.testapp.TestOrderController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestServiceSetup.class})
@AutoConfigureMockMvc
public class CustomSecurityWithDefaultAuthenticatorTest {

    public static final String TEST_VALID_USER_NAME = "saeed";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDefaultBasicAuthenticator() throws Exception {
        // Call the service without providing the user token
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/protected-api")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Call the service with a valid user encoded as HTTP basic authentication. (That's the only format that the
        // default authenticator knows)
        mockMvc.perform(get(TestOrderController.REST_BASE_PATH + "/protected-api")
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, HttpBasicAuthentication.of(TEST_VALID_USER_NAME,"pass")))
                .andExpect(status().isOk());
    }

    @SpringBootApplication(scanBasePackages = "ir.sahab.rest.common.testapp")
    @PropertySource("classpath:rest-commons-test.properties")
    @EnableJpaRepositories(basePackages = "ir.sahab.rest.common.testapp")
    @EntityScan(basePackages = "ir.sahab.rest.common.testapp")
    @EnableCustomSecurity(
            applicationBasePathPattern = "/api/**")
    public static class RestServiceSetup {
    }

}
