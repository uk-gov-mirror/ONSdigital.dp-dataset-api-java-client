package dp.api.dataset;

import com.github.onsdigital.logging.builder.LogMessageBuilder;

public class LogBuilder extends LogMessageBuilder {

    @Override
    public String getLoggerName() {
        return "dp-dataset-api-java-client";
    }

    public LogBuilder(String eventDescription) {
        super(eventDescription);
    }
}
