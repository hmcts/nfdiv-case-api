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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SERVICE_ORDER_TYPE_DEEMED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
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

        Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        var alternativeService = caseData.getAlternativeService();

        String isServiceOrderTypeDeemed;
        LocalDate serviceApplicationDecisionDate;
        if (DEEMED.equals(alternativeService.getAlternativeServiceType())) {
            isServiceOrderTypeDeemed = YES.getValue();
            serviceApplicationDecisionDate = alternativeService.getDeemedServiceDate();
        } else {
            isServiceOrderTypeDeemed = NO.getValue();
            serviceApplicationDecisionDate = alternativeService.getServiceApplicationDecisionDate();
        }

        templateContent.put(CASE_REFERENCE, ccdCaseReference);
        templateContent.put(PETITIONER_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name());
        templateContent.put(RESPONDENT_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name());
        templateContent.put(DOCUMENTS_ISSUED_ON,
            alternativeService.getServiceApplicationDecisionDate().format(DATE_TIME_FORMATTER));
        templateContent.put(SERVICE_APPLICATION_RECEIVED_DATE,
            alternativeService.getReceivedServiceApplicationDate().format(DATE_TIME_FORMATTER));
        templateContent.put(SERVICE_APPLICATION_DECISION_DATE,
            serviceApplicationDecisionDate.format(DATE_TIME_FORMATTER));
        templateContent.put(IS_SERVICE_ORDER_TYPE_DEEMED, isServiceOrderTypeDeemed);

        if (NO.equals(alternativeService.getServiceApplicationGranted())) {
            templateContent.put(REFUSAL_REASON, alternativeService.getServiceApplicationRefusalReason());
            templateContent.put(PARTNER, commonContent.getPartner(caseData, caseData.getApplicant2()));
            templateContent.put(IS_DIVORCE, caseData.isDivorce() ? YES.getValue() : NO.getValue());
            email = caseData.isDivorce() ? CONTACT_DIVORCE_JUSTICE_GOV_UK : CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
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

        templateContent.put("ctscContactDetails", ctscContactDetails);

        return templateContent;
    }
}
