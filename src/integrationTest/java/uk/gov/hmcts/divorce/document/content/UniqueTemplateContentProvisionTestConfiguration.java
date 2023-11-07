package uk.gov.hmcts.divorce.document.content;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

import java.time.Clock;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.divorce.document.content.templatecontent"})
public class UniqueTemplateContentProvisionTestConfiguration {

    @MockBean
    private ConditionalOrderCommonContent conditionalOrderCommonContent;
    @MockBean
    private Clock clock;
    @MockBean
    private DocmosisCommonContent docmosisCommonContent;
}
