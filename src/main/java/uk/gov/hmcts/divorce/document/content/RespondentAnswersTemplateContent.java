package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;

@Component
@Slf4j
public class RespondentAnswersTemplateContent {

    private static final String RESP_JURISDICTION_AGREE = "respJurisdictionAgree";
    private static final String RESP_JURISDICTION_DISAGREE_REASON = "respJurisdictionDisagreeReason";
    private static final String RESP_LEGAL_PROCEEDINGS_EXIST = "respLegalProceedingsExist";
    private static final String RESP_LEGAL_PROCEEDINGS_DESCRIPTION = "respLegalProceedingsDescription";

    public Supplier<Map<String, Object>> apply(final CaseData caseData,
                                               final Long ccdCaseReference,
                                               final LocalDate createdDate) {

        return () -> {
            log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

            Map<String, Object> templateData = new HashMap<>();
            templateData.put(ISSUE_DATE, createdDate.format(dateTimeFormatter));
            templateData.put(CCD_CASE_REFERENCE, ccdCaseReference);

            var application = caseData.getApplication();
            templateData.put(APPLICANT_1_FULL_NAME, application.getMarriageDetails().getApplicant1Name());
            templateData.put(APPLICANT_2_FULL_NAME, application.getMarriageDetails().getApplicant2Name());

            String respSolicitorRepresented = YesOrNo.from(caseData.getApplicant2().isRepresented()).getValue();
            templateData.put("respSolicitorRepresented", respSolicitorRepresented);

            var acknowledgementOfService = caseData.getAcknowledgementOfService();
            templateData.put(RESP_JURISDICTION_AGREE, acknowledgementOfService.getJurisdictionAgree().getValue());
            templateData.put(RESP_JURISDICTION_DISAGREE_REASON, acknowledgementOfService.getJurisdictionDisagreeReason());
            templateData.put(RESP_LEGAL_PROCEEDINGS_EXIST, acknowledgementOfService.getLegalProceedingsExist().getValue());
            templateData.put(RESP_LEGAL_PROCEEDINGS_DESCRIPTION, acknowledgementOfService.getLegalProceedingsDescription());

            return templateData;
        };
    }
}
