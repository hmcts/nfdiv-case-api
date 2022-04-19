package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SERVICE_ORDER_TYPE_DEEMED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.REFUSAL_REASON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_ORDER_TITLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class ServiceOrderTemplateContent {

    @Value("${court.locations.serviceCentre.serviceCentreName}")
    private String serviceCentre;

    @Value("${court.locations.serviceCentre.centreName}")
    private String centreName;

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.town}")
    private String town;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postcode;

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        String isServiceOrderTypeDeemed;
        LocalDate serviceApplicationDecisionDate;

        log.info("Generating service order template content for case reference {} and application type {} ",
            ccdCaseReference, caseData.getDivorceOrDissolution());

        Map<String, Object> templateContent = new HashMap<>();
        var alternativeService = caseData.getAlternativeService();

        if (DEEMED.equals(alternativeService.getAlternativeServiceType())) {
            isServiceOrderTypeDeemed = YES.getValue();
            serviceApplicationDecisionDate = alternativeService.getDeemedServiceDate();
        } else {
            isServiceOrderTypeDeemed = NO.getValue();
            serviceApplicationDecisionDate = alternativeService.getServiceApplicationDecisionDate();
        }

        templateContent.put(CASE_REFERENCE, ccdCaseReference);
        templateContent.put(DIVORCE_OR_DISSOLUTION, caseData.isDivorce() ? "divorce process" : "process to end your civil partnership");
        templateContent.put(PETITIONER_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(RESPONDENT_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(DOCUMENTS_ISSUED_ON,
            alternativeService.getServiceApplicationDecisionDate().format(DATE_TIME_FORMATTER));
        templateContent.put(SERVICE_APPLICATION_RECEIVED_DATE,
            alternativeService.getReceivedServiceApplicationDate().format(DATE_TIME_FORMATTER));
        templateContent.put(IS_SERVICE_ORDER_TYPE_DEEMED, isServiceOrderTypeDeemed);
        templateContent.put(SERVICE_ORDER_TITLE, isServiceOrderTypeDeemed.equals(YES.getValue())
            ? "Deemed service order" : "Order to dispense with service");
        templateContent.put(DUE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));

        if (serviceApplicationDecisionDate != null) {
            templateContent.put(SERVICE_APPLICATION_DECISION_DATE,
                serviceApplicationDecisionDate.format(DATE_TIME_FORMATTER));
        }

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .emailAddress(email)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();

        if (NO.equals(alternativeService.getServiceApplicationGranted())) {
            templateContent.put(REFUSAL_REASON, alternativeService.getServiceApplicationRefusalReason());
            templateContent.put(PARTNER, commonContent.getPartner(caseData, caseData.getApplicant2()));
            templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES.getValue() : NO.getValue());
            ctscContactDetails.setEmailAddress(
                caseData.isDivorce() ? CONTACT_DIVORCE_JUSTICE_GOV_UK : CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
        }

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }
}
