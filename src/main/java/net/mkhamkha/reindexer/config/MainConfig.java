package net.mkhamkha.reindexer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.mkhamkha.reindexer.component.TomcatErrorReportValve;
import net.mkhamkha.reindexer.config.property.ReindexerConfig;
import net.mkhamkha.reindexer.config.property.ReindexerProperty;
import net.mkhamkha.reindexer.model.Item;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.rt.restream.reindexer.NamespaceOptions;
import ru.rt.restream.reindexer.Reindexer;
import ru.rt.restream.reindexer.ReindexerConfiguration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableFeignClients(basePackages = {"net.mkhamkha.reindexer.component"})
@EnableConfigurationProperties(value = {ReindexerProperty.class, ReindexerConfig.class})
public class MainConfig {

    private final ReindexerConfig config;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // Базу данных bb необходимо создать заранее, до запуска приложения.
    @Bean
    public Reindexer reindexer() {

        // Подключение к БД
        Reindexer reindexer = ReindexerConfiguration.builder()
                .url("cproto://localhost:6534/" + config.getDbName())
                .connectionPoolSize(1)
                .requestTimeout(Duration.ofSeconds(30L))
                .getReindexer();

        // Открыть Namespace
        reindexer.openNamespace(config.getNameSpace(), NamespaceOptions.defaultOptions(), Item.class);

        return reindexer;
    }

    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> errorReportValveCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            ErrorReportValve valve = new TomcatErrorReportValve();
            context.getParent().getPipeline().addValve(valve);
        });
    }
}