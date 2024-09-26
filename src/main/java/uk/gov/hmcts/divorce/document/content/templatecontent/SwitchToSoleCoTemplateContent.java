package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class SwitchToSoleCoTemplateContent implements TemplateContent {

    private final Clock clock;
    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;

    public static final String GET_A_DIVORCE = "get a divorce";
    public static final String END_YOUR_CIVIL_PARTNERSHIP = "end your civil partnership";
    public static final String YOU_ARE_DIVORCED = "you are divorced";
    public static final String CIVIL_PARTNERSHIP_LEGALLY_ENDED = "your civil partnership is legally ended";
    public static final String DIVORCED_OR_CP_LEGALLY_ENDED = "divorcedOrCivilPartnershipLegallyEnded";

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {
        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER,
            commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()));

        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, caseData.isDivorce() ? GET_A_DIVORCE :  END_YOUR_CIVIL_PARTNERSHIP);
        templateContent.put(DIVORCED_OR_CP_LEGALLY_ENDED, caseData.isDivorce() ? YOU_ARE_DIVORCED : CIVIL_PARTNERSHIP_LEGALLY_ENDED);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, caseData.isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);
        templateContent.put(THE_APPLICATION, caseData.isDivorce() ? DIVORCE : APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);

        return templateContent;
    }
}
