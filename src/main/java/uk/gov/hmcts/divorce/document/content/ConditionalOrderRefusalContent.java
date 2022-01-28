package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.END_A_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.THE_DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
public class ConditionalOrderRefusalContent {

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final Application application = caseData.getApplication();

        templateContent.put(CASE_REFERENCE, ccdCaseReference);

        templateContent.put(ISSUE_DATE_POPULATED, application.getIssueDate() != null);
        if (application.getIssueDate() != null) {
            templateContent.put(ISSUE_DATE, application.getIssueDate().format(DATE_TIME_FORMATTER));
        }

        templateContent.put("isSole", caseData.getApplicationType().isSole());
        templateContent.put("isJoint", !caseData.getApplicationType().isSole());

        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());

        templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_A_CIVIL_PARTNERSHIP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
        }

        templateContent.put("hasLegalAdvisorComments", conditionalOrder.getRefusalRejectionAdditionalInfo());
        if (conditionalOrder.getRefusalRejectionAdditionalInfo() != null) {
            templateContent.put("legalAdvisorComments", conditionalOrder.getRefusalRejectionAdditionalInfo());
        }

        templateContent.put("phoneNumber", phoneNumber);

        return templateContent;
    }
}
