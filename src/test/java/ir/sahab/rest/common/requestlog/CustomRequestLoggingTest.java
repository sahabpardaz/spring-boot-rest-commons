package ir.sahab.rest.common.requestlog;

import static ir.sahab.rest.common.testapp.TestOrderController.REST_BASE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.sahab.rest.common.requestlog.CustomRequestLoggingTest.RestServiceSetup;
import ir.sahab.rest.common.testapp.TestOrderController;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.logging.LogLevel;
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
public class CustomRequestLoggingTest {

    private static final int MAX_BODY_SIZE = 2 * 1024;

    private static MemoryAppender memoryAppender;

    @Autowired
    private MockMvc mockMvc;

    @BeforeClass
    public static void setUpClass() {
        Logger.getRootLogger().setLevel(Level.TRACE);
        memoryAppender = new MemoryAppender();
        Logger.getLogger(HttpRequestLogWriter.class.getCanonicalName()).addAppender(memoryAppender);
    }

    @AfterClass
    public static void tearDownClass() {
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger(HttpRequestLogWriter.class.getCanonicalName()).removeAllAppenders();
    }

    @Before
    public void setUp() {
        memoryAppender.reset();
    }

    @Test
    public void testRequestLoggingFields() throws Exception {
        List<LoggingEvent> loggedEvents = memoryAppender.getLoggedEvents();
        assertEquals(0, loggedEvents.size());

        mockMvc.perform(post(REST_BASE_PATH + "/secureInsert?SECURE_ID=55&name=soap&SECURE_COUNT=10")
                .accept(MediaType.APPLICATION_JSON)
                .header("SECURE_HEADER", "1234"))
                .andExpect(status().isOk());

        assertEquals(2, loggedEvents.size());
        List<String> types = memoryAppender.getFieldValue("type", Level.TRACE);
        assertEquals(2, types.size());
        assertTrue(types.contains("request"));
        assertTrue(types.contains("response"));

        // Check request URI with obfuscated parameter
        List<String> uris = memoryAppender.getRequestFieldValue("uri", Level.TRACE);
        assertEquals(1, uris.size());
        assertEquals("http://localhost" + REST_BASE_PATH + "/secureInsert?SECURE_ID=XXX&name=soap&SECURE_COUNT=XXX",
                uris.get(0));

        // Check request headers with obfuscated header
        List<String> headers = memoryAppender.getRequestFieldValue("headers", Level.TRACE);
        assertEquals(1, headers.size());
        assertTrue(headers.get(0).contains("SECURE_HEADER=[\"XXX\"]"));

        // Check request HTTP method
        List<String> methods = memoryAppender.getRequestFieldValue("method", Level.TRACE);
        assertEquals(1, methods.size());
        assertEquals("POST", methods.get(0));

        // Check request/response's correlation
        List<String> correlations = memoryAppender.getRequestFieldValue("correlation", Level.TRACE);
        assertEquals(1, correlations.size());
        String requestCorrelation = correlations.get(0);
        correlations = memoryAppender.getResponseFieldValue("correlation", Level.TRACE);
        assertEquals(1, correlations.size());
        String responseCorrelation = correlations.get(0);
        assertEquals(requestCorrelation, responseCorrelation);

        // Check response's status
        List<String> status = memoryAppender.getResponseFieldValue("status", Level.TRACE);
        assertEquals(1, status.size());
        assertEquals("200", status.get(0));
    }

    @Test
    public void testMaxBodySizeConfig() throws Exception {
        List<LoggingEvent> loggedEvents = memoryAppender.getLoggedEvents();
        assertEquals(0, loggedEvents.size());

        // Check normal case that the request and response's body size are less than the configured max body size.
        String requestBody = RandomStringUtils.random(MAX_BODY_SIZE - 200, true, true);
        mockMvc.perform(post(TestOrderController.REST_BASE_PATH + "/echo")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.TEXT_PLAIN)
                .content(requestBody))
                .andExpect(status().isOk());
        checkRequestAndResponseBody(requestBody);

        // Check corner case that the request and response's body size are bigger than the configured max body size.
        memoryAppender.reset();
        requestBody = RandomStringUtils.random(MAX_BODY_SIZE + 200, true, true);
        mockMvc.perform(post(TestOrderController.REST_BASE_PATH + "/echo")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.TEXT_PLAIN)
                .content(requestBody))
                .andExpect(status().isOk());
        checkRequestAndResponseBody(requestBody);
    }

    private void checkRequestAndResponseBody(String actualRequestBody) {
        // Truncate body to maxBodySize and append 3 dots
        String truncatedBody = StringUtils.left(actualRequestBody, MAX_BODY_SIZE);
        if (!truncatedBody.equals(actualRequestBody)) {
            truncatedBody += "...";
        }

        // Check request body
        List<String> requestBodyList = memoryAppender.getRequestFieldValue("body", Level.TRACE);
        assertEquals(1, requestBodyList.size());
        String requestBody = requestBodyList.get(0);
        assertEquals(truncatedBody.length(), requestBody.length());

        // Check response body
        List<String> responseBodyList = memoryAppender.getResponseFieldValue("body", Level.TRACE);
        assertEquals(1, responseBodyList.size());
        String responseBody = responseBodyList.get(0);
        assertEquals(truncatedBody.length(), responseBody.length());
        assertEquals(truncatedBody, requestBody);
        assertEquals(requestBody, responseBody);
    }

    @SpringBootApplication(scanBasePackages = "ir.sahab.rest.common.testapp")
    @PropertySource("classpath:rest-commons-test.properties")
    @EnableJpaRepositories(basePackages = "ir.sahab.rest.common.testapp")
    @EntityScan(basePackages = "ir.sahab.rest.common.testapp")
    @EnableCustomRequestLogging(obfuscateHeaders = "SECURE_HEADER", obfuscateParameters = "SECURE_ID,SECURE_COUNT",
            maxBodySize = MAX_BODY_SIZE, logLevel = LogLevel.TRACE)
    public static class RestServiceSetup extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/**");
        }

    }
}

