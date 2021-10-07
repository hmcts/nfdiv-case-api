package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class RespondentAnswersTemplateContent {

    private static final String RESP_JURISDICTION_AGREE = "respJurisdictionAgree";
    private static final String RESP_JURISDICTION_DISAGREE_REASON = "respJurisdictionDisagreeReason";
    private static final String RESP_LEGAL_PROCEEDINGS_EXIST = "respLegalProceedingsExist";
    private static final String RESP_LEGAL_PROCEEDINGS_DESCRIPTION = "respLegalProceedingsDescription";
    private static final String RESP_SOLICITOR_REPRESENTED = "respSolicitorRepresented";

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     final LocalDate createdDate) {

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(ISSUE_DATE, createdDate.format(DATE_TIME_FORMATTER));
        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);

        var application = caseData.getApplication();
        templateContent.put(APPLICANT_1_FULL_NAME, application.getMarriageDetails().getApplicant1Name());
        templateContent.put(APPLICANT_2_FULL_NAME, application.getMarriageDetails().getApplicant2Name());

        String respSolicitorRepresented = YesOrNo.from(caseData.getApplicant2().isRepresented()).getValue();
        templateContent.put(RESP_SOLICITOR_REPRESENTED, respSolicitorRepresented);

        var acknowledgementOfService = caseData.getAcknowledgementOfService();
        templateContent.put(RESP_JURISDICTION_AGREE, acknowledgementOfService.getJurisdictionAgree().getValue());
        templateContent.put(RESP_JURISDICTION_DISAGREE_REASON, acknowledgementOfService.getJurisdictionDisagreeReason());
        templateContent.put(RESP_LEGAL_PROCEEDINGS_EXIST, acknowledgementOfService.getLegalProceedingsExist().getValue());
        templateContent.put(RESP_LEGAL_PROCEEDINGS_DESCRIPTION, acknowledgementOfService.getLegalProceedingsDescription());

        return templateContent;
    }
}
