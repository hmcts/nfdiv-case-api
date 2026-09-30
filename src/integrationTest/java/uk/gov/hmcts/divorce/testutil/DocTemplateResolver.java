package uk.gov.hmcts.divorce.testutil;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

@Component
public final class DocTemplateResolver {

    private final DocmosisTemplatesConfig docmosisTemplatesConfig;

    public DocTemplateResolver(DocmosisTemplatesConfig docmosisTemplatesConfig) {
        this.docmosisTemplatesConfig = docmosisTemplatesConfig;
    }

    public String resolveTemplateID(String templateId) {
        return resolveTemplateID(templateId, LanguagePreference.ENGLISH);
    }

    public String resolveTemplateID(String templateId, LanguagePreference languagePreference) {
        return docmosisTemplatesConfig.getTemplates()
            .get(languagePreference)
            .get(templateId);
    }
}
