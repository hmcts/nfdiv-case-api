package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SERVICE_ORDER_TYPE_DEEMED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.REFUSAL_REASON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class ServiceOrderTemplateContent {

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        log.info("Generating service order template content for case reference {} and application type {} ",
            ccdCaseReference, caseData.getDivorceOrDissolution());

        Map<String, Object> templateContent = new HashMap<>();
        var alternativeService = caseData.getAlternativeService();

        LocalDate serviceApplicationDecisionDate;

        if (DEEMED.equals(alternativeService.getAlternativeServiceType())) {
            serviceApplicationDecisionDate = alternativeService.getDeemedServiceDate();
        } else {
            serviceApplicationDecisionDate = alternativeService.getServiceApplicationDecisionDate();
        }

        templateContent.put(CASE_REFERENCE, caseData.formatCaseRef(ccdCaseReference));

        final LanguagePreference applicantLanguagePreference = caseData.getApplicant1().getLanguagePreference();

        if (caseData.isDivorce()) {
            templateContent.put(DIVORCE_OR_DISSOLUTION,
                WELSH.equals(applicantLanguagePreference)
                    ? DIVORCE_PROCESS_CY
                    : DIVORCE_PROCESS
            );
        } else {
            templateContent.put(DIVORCE_OR_DISSOLUTION,
                WELSH.equals(applicantLanguagePreference)
                    ? PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY
                    : PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP
            );
        }

        templateContent.put(PETITIONER_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(RESPONDENT_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(DOCUMENTS_ISSUED_ON,
            alternativeService.getServiceApplicationDecisionDate().format(DATE_TIME_FORMATTER));
        templateContent.put(SERVICE_APPLICATION_RECEIVED_DATE,
            alternativeService.getReceivedServiceApplicationDate().format(DATE_TIME_FORMATTER));
        templateContent.put(IS_SERVICE_ORDER_TYPE_DEEMED, DEEMED.equals(alternativeService.getAlternativeServiceType()) ? YES : NO);

        if (serviceApplicationDecisionDate != null) {
            templateContent.put(SERVICE_APPLICATION_DECISION_DATE,
                serviceApplicationDecisionDate.format(DATE_TIME_FORMATTER));
        }

        if (alternativeService.isApplicationGranted()) {
            if (caseData.getDueDate() != null) {
                templateContent.put(DUE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
            }
        } else {
            templateContent.put(REFUSAL_REASON, alternativeService.getServiceApplicationRefusalReason());
            templateContent.put(PARTNER,
                commonContent.getPartner(caseData, caseData.getApplicant2(), caseData.getApplicant1().getLanguagePreference()));
            templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES : NO);
        }

        templateContent.put(CTSC_CONTACT_DETAILS, CtscContactDetails
            .builder()
            .emailAddress(email)
            .phoneNumber(phoneNumber)
            .build());

        return templateContent;
    }
}
