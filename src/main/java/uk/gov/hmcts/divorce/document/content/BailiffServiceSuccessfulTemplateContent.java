package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
@Slf4j
public class BailiffServiceSuccessfulTemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    private final CommonContent commonContent;

    public static final String CoS_DATE = "cos_date";

    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(RECIPIENT_NAME,  applicant.getFullName());
        templateContent.put(RECIPIENT_ADDRESS,  AddressUtil.getPostalAddress(applicant.getAddress()));
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(CoS_DATE, caseData.getAlternativeService().getBailiff().getCertificateOfServiceDate()
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        templateContent.put(DUE_DATE, caseData.getDueDate()
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2(), applicant.getLanguagePreference()));

        return templateContent;
    }
}
