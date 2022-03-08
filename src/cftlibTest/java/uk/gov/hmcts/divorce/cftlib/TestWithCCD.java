package uk.gov.hmcts.divorce.cftlib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestWithCCD extends CftlibTest {

    // Unable to use mockmvc
    // https://github.com/Microsoft/ApplicationInsights-Java/issues/895
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void isHealthy() throws Exception {
        var x = this.restTemplate.getForObject("http://localhost:4013/health", String.class);
        assertThat(x).contains("OK");
    }

}
