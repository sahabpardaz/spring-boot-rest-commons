package ir.sahab.rest.common.requestlog;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Apache Log4j appender that stores all the logging events in an in-memory array.
 */
public class MemoryAppender extends AppenderSkeleton {

    public List<LoggingEvent> events = new ArrayList<>();

    public List<LoggingEvent> getLoggedEvents() {
        return Collections.unmodifiableList(this.events);
    }

    @Override
    protected void append(LoggingEvent event) {
        events.add(event);
    }

    @Override
    public void close() {
        this.events = null;
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public void reset() {
        this.events = new ArrayList<>();
    }

    public List<String> getRequestFieldValue(String fieldName, Level level) {
        return getFieldValue("request", fieldName, level);
    }

    public List<String> getResponseFieldValue(String fieldName, Level level) {
        return getFieldValue("response", fieldName, level);
    }

    public List<String> getFieldValue(String fieldName, Level level) {
        return getFieldValue(null, fieldName, level);
    }

    private List<String> getFieldValue(String type, String fieldName, Level level) {
        return this.events.stream()
                .filter(event -> event.getLevel().equals(level))
                .filter(event -> {
                    if (type == null) {
                        return true;
                    }
                    return JsonPath.parse(event.getMessage().toString()).read("$.type").toString()
                            .equalsIgnoreCase(type);
                })
                .map(event -> JsonPath.parse(event.getMessage().toString()).read("$." + fieldName).toString())
                .filter(Objects::nonNull).collect(Collectors.toList());
    }
}