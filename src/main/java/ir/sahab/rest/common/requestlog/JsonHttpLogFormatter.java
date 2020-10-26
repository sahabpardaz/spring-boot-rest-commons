package ir.sahab.rest.common.requestlog;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpMessage;
import org.zalando.logbook.StructuredHttpLogFormatter;

/**
 * A custom {@link HttpLogFormatter} that produces JSON objects.
 */
public class JsonHttpLogFormatter implements StructuredHttpLogFormatter {

    static final Predicate<String> JSON = contentType -> {
        if (contentType == null) {
            return false;
        }
        // implementation note: manually coded for improved performance
        if (contentType.startsWith("application/")) {
            int index = contentType.indexOf(';', 12);
            if (index != -1) {
                if (index > 16) {
                    // application/some+json;charset=utf-8
                    return contentType.regionMatches(index - 5, "+json", 0, 5);
                }

                // application/json;charset=utf-8
                return contentType.regionMatches(index - 4, "json", 0, 4);
            } else {
                // application/json
                if (contentType.length() == 16) {
                    return contentType.endsWith("json");
                }
                // application/some+json
                return contentType.endsWith("+json");
            }
        }
        return false;
    };

    private final ObjectMapper mapper;

    public JsonHttpLogFormatter() {
        this(new ObjectMapper());
    }

    public JsonHttpLogFormatter(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Object> prepareBody(final HttpMessage message) throws IOException {
        final String contentType = message.getContentType();
        final String body = message.getBodyAsString();
        if (body.isEmpty()) {
            return Optional.empty();
        }
        if (JSON.test(contentType) && !body.endsWith("...")) {
            // TODO has this JSON been validated? If not then this might result in invalid log statements
            return Optional.of(new JsonBody(body));
        } else {
            return Optional.of(body);
        }
    }

    @Override
    public String format(final Map<String, Object> content) throws IOException {
        return mapper.writeValueAsString(content);
    }

    private static final class JsonBody {

        String json;

        public JsonBody(String json) {
            this.json = json;
        }

        @JsonRawValue
        @JsonValue
        public String getJson() {
            return json;
        }
    }

}
