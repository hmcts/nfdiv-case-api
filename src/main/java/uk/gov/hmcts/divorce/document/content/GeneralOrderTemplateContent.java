package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_TYPE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class GeneralOrderTemplateContent {

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

    public Supplier<Map<String, Object>> apply(final CaseData caseData,
                                               final Long ccdCaseReference) {

        return () -> {
            Map<String, Object> templateData = new HashMap<>();

            log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

            var generalOrder = caseData.getGeneralOrder();
            templateData.put(GENERAL_ORDER_DATE, generalOrder.getGeneralOrderDate().format(DATE_TIME_FORMATTER));
            templateData.put(CASE_REFERENCE, ccdCaseReference);
            templateData.put(PETITIONER_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name());
            templateData.put(RESPONDENT_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name());
            templateData.put(GENERAL_ORDER_RECITALS, generalOrder.getGeneralOrderRecitals());
            templateData.put(JUDGE_TYPE, generalOrder.getGeneralOrderJudgeType().getLabel());
            templateData.put(JUDGE_NAME, generalOrder.getGeneralOrderJudgeName());
            templateData.put(GENERAL_ORDER_DETAILS, generalOrder.getGeneralOrderDetails());

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

            templateData.put("ctscContactDetails", ctscContactDetails);

            return templateData;
        };
    }
}
