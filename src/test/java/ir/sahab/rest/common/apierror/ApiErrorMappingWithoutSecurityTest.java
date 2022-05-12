package ir.sahab.rest.common.apierror;

import static ir.sahab.rest.common.testapp.OrderErrorCode.NOT_AVAILABLE_IN_STORE;
import static ir.sahab.rest.common.testapp.TestOrderController.REST_BASE_PATH;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.sahab.rest.common.apierror.ApiErrorMappingWithoutSecurityTest.RestServiceSetup;
import ir.sahab.rest.common.testapp.TestOrderEntity;
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
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestServiceSetup.class})
@AutoConfigureMockMvc
public class ApiErrorMappingWithoutSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSpringMvcExceptions() throws Exception {
        // Invalid path variable type
        mockMvc.perform(get(REST_BASE_PATH + "/order/wrongLongId")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.detail").exists())
                // User will be filled if you enable security via @EnableCustomSecurity
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "/order/wrongLongId"))
                .andExpect(jsonPath("$.http_method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message").value(containsString("Bad Request: Failed to convert value")))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // Required query parameter
        mockMvc.perform(get(REST_BASE_PATH + "/searchByName")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "/searchByName"))
                .andExpect(jsonPath("$.http_method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message").value("Bad Request: "
                        + "Required request parameter 'name' for method parameter type String is not present"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // Required request body
        mockMvc.perform(post(REST_BASE_PATH)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH))
                .andExpect(jsonPath("$.http_method").value("POST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message")
                        .value(containsString("Bad Request: Required request body is missing")))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // Method not allowed
        mockMvc.perform(delete(REST_BASE_PATH)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method Not Allowed"))
                .andExpect(jsonPath("$.status").value("405"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH))
                .andExpect(jsonPath("$.http_method").value("DELETE"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message").value("Method Not Allowed: Request method 'DELETE' not supported"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // No handler found
        // Note that to make it work we have to set these properties in Spring:
        // spring.mvc.throw-exception-if-no-handler-found=true
        // spring.resources.add-mappings=false
        mockMvc.perform(get(REST_BASE_PATH + "-notfound")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value("404"))
                .andExpect(jsonPath("$.detail").value("No handler found for GET /api/orders/v1-notfound"))
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "-notfound"))
                .andExpect(jsonPath("$.http_method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message")
                        .value("Not Found: No handler found for GET /api/orders/v1-notfound"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));
    }

    @Test
    public void testGeneralException() throws Exception {
        // IOException: for example Connection refused
        mockMvc.perform(get(REST_BASE_PATH + "/throwIOException")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value("500"))
                .andExpect(jsonPath("$.detail").value("Connection refused (Connection refused)"))
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "/throwIOException"))
                .andExpect(jsonPath("$.http_method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message")
                        .value("Internal Server Error: Connection refused (Connection refused)"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // Check DataIntegrityViolationException: for example DB field length constraint
        // This is a generic data exception typically thrown by the Spring exception translation mechanism when dealing
        // with lower level persistence exceptions. Although we expect to see 400 error code in this condition,
        // the default Spring mappings generates a 500 error code for this exception.
        // There are three possible Hibernate exceptions that may cause the DataIntegrityViolationException to be
        // thrown:
        // - org.hibernate.exception.ConstraintViolationException
        // - org.hibernate.PropertyValueException
        // - org.hibernate.exception.DataException
        // There is a single JPA exception that may trigger a DataIntegrityViolationException to be thrown:
        // – the javax.persistence.EntityExistsException
        TestOrderEntity entity = new TestOrderEntity(1L, "NameBiggerThan15Character"/* Invalid name length*/, 1);
        mockMvc.perform(post(REST_BASE_PATH)
                .content(entity.toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value("500"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH))
                .andExpect(jsonPath("$.http_method").value("POST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message")
                        .value(containsString("Internal Server Error: could not execute batch;")))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // Check ConstraintViolationException
        mockMvc.perform(post(REST_BASE_PATH)
                .content(new TestOrderEntity(1L, "shampoo", 20 /* Invalid count*/).toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Constraint Violation"))
                .andExpect(jsonPath("$.status").value("400"))
                .andExpect(jsonPath("$.type").value("https://zalando.github.io/problem/constraint-violation"))
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH))
                .andExpect(jsonPath("$.http_method").value("POST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message").value("Constraint Violation"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));

        // Check an unknown exception which is not mapped by default.
        mockMvc.perform(post(REST_BASE_PATH + "/throwMyException")
                .content(new TestOrderEntity(1L, null /* Invalid empty name*/, 1).toJson())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value("500"))
                .andExpect(jsonPath("$.detail").value("Name is empty!"))
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "/throwMyException"))
                .andExpect(jsonPath("$.http_method").value("POST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.en_message").value("Internal Server Error: Name is empty!"))
                .andExpect(jsonPath("$.fa_message").value("درخواست مربوطه با خطا مواجه شد."));
    }

    @Test
    public void testApiException() throws Exception {
        String productName = "NOT_AVAILABLE";
        mockMvc.perform(get(REST_BASE_PATH + "/searchByName?name=" + productName)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value(NOT_AVAILABLE_IN_STORE.getHttpStatusCode().getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(NOT_AVAILABLE_IN_STORE.getHttpStatusCode().getStatusCode()))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.user").value(""))
                .andExpect(jsonPath("$.api_path").value(REST_BASE_PATH + "/searchByName"))
                .andExpect(jsonPath("$.http_method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.tracking_id").exists())
                .andExpect(jsonPath("$.error_code").value(NOT_AVAILABLE_IN_STORE.name()))
                .andExpect(jsonPath("$.en_message")
                        .value(String.format(NOT_AVAILABLE_IN_STORE.getEnMessage(), productName)))
                .andExpect(jsonPath("$.fa_message")
                        .value(String.format(NOT_AVAILABLE_IN_STORE.getFaMessage(), productName)));
    }

    @SpringBootApplication(scanBasePackages = "ir.sahab.rest.common.testapp")
    @PropertySource("classpath:rest-commons-test.properties")
    @EnableJpaRepositories(basePackages = "ir.sahab.rest.common.testapp")
    @EntityScan(basePackages = "ir.sahab.rest.common.testapp")
    @EnableApiErrorMapping
    public static class RestServiceSetup extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/**");
        }
    }
}
