package ir.sahab.rest.common;

import static org.springframework.test.util.AssertionErrors.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * Result matcher utility which is used in unit tests to match an expected result when content is collection of a type.
 *
 * @param <T> the type of elements held in the expected collection
 */
public class CollectionContentResultMatchers<T> {

    private Class<T> clazz;

    /**
     * Creates a result matcher for {@link MockMvc} to match collection of a specific type.
     */
    private CollectionContentResultMatchers(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <T> CollectionContentResultMatchers<T> collectionContent(Class<T> clazz) {
        return new CollectionContentResultMatchers<>(clazz);
    }

    /**
     * Checks that the collection on specified JSON path meets the given predicate.
     */
    public ResultMatcher check(String jsonPath, Predicate<Collection<T>> predicate) {
        return result -> {
            String contentAsString = result.getResponse().getContentAsString();
            DocumentContext jsonContext = JsonPath.parse(contentAsString);
            @SuppressWarnings("unchecked")
            Collection<Object> objectsInPath = jsonContext.read(jsonPath, Collection.class);
            Collection<T> resultObjects = new HashSet<>();
            if (objectsInPath != null && !objectsInPath.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                for (Object objectInPath : objectsInPath) {
                    resultObjects.add(objectMapper.convertValue(objectInPath, clazz));
                }
            }
            assertEquals("Check is not passed", true, predicate.test(resultObjects));
        };
    }

    /**
     * Checks that the collection on specified JSON path contains the given entities.
     */
    public ResultMatcher object(String jsonPath, Collection<T> entities) {
        return check(jsonPath, resultObjects -> new HashSet<>(entities).equals(resultObjects));
    }

    /**
     * Checks that the collection on root of the JSON, contains the given entities.
     */
    public ResultMatcher object(Collection<T> entities) {
        return object("$", entities);
    }
}
