package uk.gov.hmcts.reform.divorce.caseapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CaseApiApplication.class, args);
    }
}
