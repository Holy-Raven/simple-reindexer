package net.mkhamkha.reindexer.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "reindexer-property")
public class ReindexerProperty {

    private String baseUrl;
    private String filePath;
}
