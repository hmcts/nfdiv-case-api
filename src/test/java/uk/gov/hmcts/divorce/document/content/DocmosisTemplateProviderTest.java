package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.WELSH_TEMPLATE_ID;

@ExtendWith(MockitoExtension.class)
class DocmosisTemplateProviderTest {

    @Mock
    private DocmosisTemplatesConfig docmosisTemplatesConfig;

    @InjectMocks
    private DocmosisTemplateProvider docmosisTemplateProvider;

    @Test
    void shouldReturnTemplateForEnglish() {

        mockDocmosisTemplateConfig();

        final String name = docmosisTemplateProvider.templateNameFor(DIVORCE_DRAFT_APPLICATION, ENGLISH);

        assertThat(name, is(ENGLISH_TEMPLATE_ID));
    }

    @Test
    void shouldReturnTemplateForWelsh() {

        mockDocmosisTemplateConfig();

        final String name = docmosisTemplateProvider.templateNameFor(DIVORCE_DRAFT_APPLICATION, WELSH);

        assertThat(name, is(WELSH_TEMPLATE_ID));
    }

    private void mockDocmosisTemplateConfig() {
        when(docmosisTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(
                    DIVORCE_DRAFT_APPLICATION, ENGLISH_TEMPLATE_ID
                ),
                WELSH, Map.of(
                    DIVORCE_DRAFT_APPLICATION, WELSH_TEMPLATE_ID
                )
            )
        );
    }
}
