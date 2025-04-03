package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

@Component
@RequiredArgsConstructor
public class DocmosisTemplateProvider {

    private final DocmosisTemplatesConfig docmosisTemplatesConfig;

    public String templateNameFor(final String templateId, final LanguagePreference languagePreference) {
        return docmosisTemplatesConfig.getTemplates().get(languagePreference).get(templateId);
    }
}
