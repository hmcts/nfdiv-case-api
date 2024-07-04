package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class SwitchToSoleCoJSTemplateContent implements TemplateContent {

    private final Clock clock;
    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {
        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant2().getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());
        templateContent.put(FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(PARTNER,
            commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()));
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));

        return templateContent;
    }
}
