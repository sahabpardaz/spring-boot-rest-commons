package ir.sahab.rest.common;

import static org.springframework.test.util.AssertionErrors.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.function.Predicate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Result matcher utility which is used in unit test to match an expected result when content is instance of a type.
 *
 * @param <T> the type of expected object
 */
public class ContentResultMatchers<T> {

    private Class<T> clazz;

    /**
     * Creates a result matcher for {@link MockMvc} to match instance of a specific type.
     */
    private ContentResultMatchers(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <T> ContentResultMatchers<T> content(Class<T> clazz) {
        return new ContentResultMatchers<>(clazz);
    }

    /**
     * Checks that the element on specified JSON path meets the given predicate.
     */
    public ResultMatcher check(String jsonPath, Predicate<T> predicate) {
        return result -> {
            String contentAsString = result.getResponse().getContentAsString();
            DocumentContext jsonContext = JsonPath.parse(contentAsString);
            @SuppressWarnings("unchecked")
            Object objectInPath = jsonContext.read(jsonPath, Object.class);
            T resultObject = null;
            if (objectInPath != null) {
                resultObject = new ObjectMapper().convertValue(objectInPath, clazz);
            }
            assertEquals("Check is not passed", true, predicate.test(resultObject));
        };
    }

    /**
     * Checks that the element on root of the JSON meets the given predicate.
     */
    public ResultMatcher check(Predicate<T> predicate) {
        return check("$", predicate);
    }

    /**
     * Checks that the element on root of the JSON is equal to the given entity.
     */
    public ResultMatcher object(T entity) {
        return check(resultObject -> resultObject.equals(entity));
    }
}
