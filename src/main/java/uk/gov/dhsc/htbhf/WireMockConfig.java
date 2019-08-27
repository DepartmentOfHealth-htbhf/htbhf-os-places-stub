package uk.gov.dhsc.htbhf;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;
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
                .fileSource(new ClasspathFileSourceWithoutLeadingSlash());
    }
}
