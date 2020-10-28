package ir.sahab.rest.common.requestlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

/**
 * By default the logbook module, logs all HTTP request/response by TRACE level, but we want to have our custom log
 * level (INFO by default). This class changes the log level.
 */
public final class HttpRequestLogWriter implements HttpLogWriter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLogWriter.class);

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void write(final Precorrelation precorrelation, final String request) {
        logRequestResponse(request);
    }

    @Override
    public void write(final Correlation correlation, final String response) {
        logRequestResponse(response);
    }

    private void logRequestResponse(String data) {
        LogLevel logLevel = CustomRequestLoggingMetadata.getInstance().getLogLevel();
        switch (logLevel) {
            case ERROR:
            case FATAL:
                logger.error(data);
                break;
            case WARN:
                logger.warn(data);
                break;
            case DEBUG:
                logger.debug(data);
                break;
            case TRACE:
                logger.trace(data);
                break;
            default:
                logger.info(data);
        }
    }
}
