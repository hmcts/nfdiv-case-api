package uk.gov.hmcts.divorce.document.content;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(UniqueTemplateContentProvisionTestConfiguration.class)
@TestPropertySource(properties = { "spring.config.location=file:src/integrationTest/resources/application.yaml" })
@ContextConfiguration(initializers= ConfigDataApplicationContextInitializer.class)
public class UniqueTemplateContentProvisionIT {

    @Autowired
    private List<TemplateContent> templateContent;

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
