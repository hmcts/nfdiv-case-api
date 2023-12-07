package uk.gov.hmcts.divorce.document.content;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(UniqueTemplateContentProvisionTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
