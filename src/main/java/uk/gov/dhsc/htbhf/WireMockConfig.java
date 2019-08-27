package uk.gov.dhsc.htbhf;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Configuration
public class WireMockConfig {

    @Value("${server.port}")
    private int port;

    @PostConstruct
    public void startWiremock() {
        WireMockServer wireMockServer = new WireMockServer(wiremockOptions());
        wireMockServer.start();
    }

    private WireMockConfiguration wiremockOptions() {
        return options()
                .port(port)
                // work around for wiremock not reading from classpath of spring boot jars.
                // see https://github.com/tomakehurst/wiremock/issues/725
                .fileSource(new ClasspathFileSource("BOOT-INF/classes"))
                .usingFilesUnderDirectory("src/main/resources");
    }
}
