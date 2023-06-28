package uk.gov.hmcts.divorce.cftlib.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Component
public class Wiremock {
    WireMockServer server;

    @PostConstruct
    void init() {
        server = new WireMockServer(options()
            .port(8765)
            .usingFilesUnderClasspath("wiremock"));
        server.start();
        System.out.println("Wiremock started on port " + server.port());
    }
}
