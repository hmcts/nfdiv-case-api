package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.A_DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPUTING_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPUTING_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DISPUTING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WITHOUT_DISPUTING_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WITHOUT_DISPUTING_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class RespondentAnswersTemplateContent {

    private static final String RESP_JURISDICTION_AGREE = "respJurisdictionAgree";
    private static final String REASON_HAVE_NO_JURISDICTION = "reasonCourtsOfEnglandAndWalesHaveNoJurisdiction";
    private static final String IN_WHICH_COUNTRY_IS_YOUR_LIFE_MAINLY_BASED = "inWhichCountryIsYourLifeMainlyBased";
    private static final String RESP_LEGAL_PROCEEDINGS_EXIST = "respLegalProceedingsExist";
    private static final String RESP_LEGAL_PROCEEDINGS_DESCRIPTION = "respLegalProceedingsDescription";
    private static final String RESP_SOLICITOR_REPRESENTED = "respSolicitorRepresented";

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(CCD_CASE_REFERENCE, formatId(ccdCaseReference));

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        String respSolicitorRepresented = YesOrNo.from(caseData.getApplicant2().isRepresented()).getValue();
        templateContent.put(RESP_SOLICITOR_REPRESENTED, respSolicitorRepresented);

        var acknowledgementOfService = caseData.getAcknowledgementOfService();
        templateContent.put(RESP_JURISDICTION_AGREE, acknowledgementOfService.getJurisdictionAgree().getValue());
        templateContent.put(REASON_HAVE_NO_JURISDICTION, acknowledgementOfService.getReasonCourtsOfEnglandAndWalesHaveNoJurisdiction());
        templateContent.put(IN_WHICH_COUNTRY_IS_YOUR_LIFE_MAINLY_BASED, acknowledgementOfService.getInWhichCountryIsYourLifeMainlyBased());
        templateContent.put(RESP_LEGAL_PROCEEDINGS_EXIST, caseData.getApplicant2().getLegalProceedings().getValue());
        templateContent.put(RESP_LEGAL_PROCEEDINGS_DESCRIPTION, caseData.getApplicant2().getLegalProceedingsDetails());

        if (caseData.isDivorce()) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateContent.put(THE_APPLICATION, A_DIVORCE_APPLICATION);

            if (caseData.getAcknowledgementOfService().isDisputed()) {
                templateContent.put(IS_DISPUTING, DISPUTING_DIVORCE);
            } else {
                templateContent.put(IS_DISPUTING, WITHOUT_DISPUTING_DIVORCE);
            }
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(THE_APPLICATION, END_A_CIVIL_PARTNERSHIP);

            if (caseData.getAcknowledgementOfService().isDisputed()) {
                templateContent.put(IS_DISPUTING, DISPUTING_CIVIL_PARTNERSHIP);
            } else {
                templateContent.put(IS_DISPUTING, WITHOUT_DISPUTING_CIVIL_PARTNERSHIP);
            }
        }

        return templateContent;
    }
}
