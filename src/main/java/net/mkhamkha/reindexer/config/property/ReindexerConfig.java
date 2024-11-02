package net.mkhamkha.reindexer.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "reindexer-config")
public class ReindexerConfig {

    private String dbName;
    private String nameSpace;
}
