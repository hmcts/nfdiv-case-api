package uk.gov.hmcts.divorce.document.content;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.common.config.ConditionalOrderCourtDetailsConfig;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(UniqueTemplateContentProvisionTestConfiguration.class)
public class UniqueTemplateContentProvisionIT {

    @Autowired
    private List<TemplateContent> templateContent;

    @MockBean
    private GenerateCertificateOfEntitlementHelper generateCertificateOfEntitlementHelper;

    @MockBean
    private ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;

    @Test
    public void shouldOnlyProvideForEachTemplateOnce() {
        long nonDistinctSum = templateContent.stream()
            .map(TemplateContent::getSupportedTemplates)
            .mapToLong(Collection::size)
            .sum();

        long distinctSum = templateContent.stream()
            .map(TemplateContent::getSupportedTemplates)
            .flatMap(Collection::stream)
            .distinct()
            .count();

        assertThat(nonDistinctSum).isEqualTo(distinctSum);
    }
}
