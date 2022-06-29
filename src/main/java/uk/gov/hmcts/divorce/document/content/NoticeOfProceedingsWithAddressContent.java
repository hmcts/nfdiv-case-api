package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPLAY_HEADER_ADDRESS;

@Component
@Slf4j
public class NoticeOfProceedingsWithAddressContent {

    @Autowired
    private NoticeOfProceedingContent noticeOfProceedingContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     final Applicant partner,
                                     final LanguagePreference languagePreference) {

        final Map<String, Object> templateContent = noticeOfProceedingContent.apply(
            caseData,
            ccdCaseReference,
            partner,
            languagePreference);
        templateContent.put(DISPLAY_HEADER_ADDRESS, true);

        return templateContent;
    }
}
