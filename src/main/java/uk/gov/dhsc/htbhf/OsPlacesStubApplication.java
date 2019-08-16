package uk.gov.dhsc.htbhf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(WireMockConfig.class)
public class OsPlacesStubApplication {

    public static void main(String[] args) {
        SpringApplication.run(OsPlacesStubApplication.class, args);
    }
}
