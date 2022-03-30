package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_REGISTERED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WHO_APPLIED;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class NoticeOfProceedingApplicantSolicitorContent {

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        Applicant applicant1 = caseData.getApplicant1();
        Applicant applicant2 = caseData.getApplicant2();
        Solicitor applicant1Solicitor = applicant1.getSolicitor();
        LocalDate applicationIssueDate = caseData.getApplication().getIssueDate();
        boolean isJoint = !caseData.getApplicationType().isSole();

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
        templateContent.put(ISSUE_DATE, applicationIssueDate.format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, isJoint);
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(WHO_APPLIED, isJoint ? "Applicant's solicitor" : "Applicants solicitor");
        templateContent.put(APPLICANT_SOLICITOR_REGISTERED, isNull(applicant1Solicitor.getOrganisationPolicy()));
        templateContent.put(SOLICITOR_NAME, applicant1Solicitor.getName());
        templateContent.put(SOLICITOR_ADDRESS, applicant1Solicitor.getAddress());

        templateContent.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant1Solicitor.getReference()) ? applicant1Solicitor.getReference() : NOT_PROVIDED
        );

        templateContent.put(
            APPLICANT_1_SOLICITOR_NAME,
            applicant1.isRepresented() ? applicant1Solicitor.getName() : NOT_REPRESENTED
        );

        templateContent.put(
            DUE_DATE,
            holdingPeriodService.getRespondByDateFor(applicationIssueDate).format(DATE_TIME_FORMATTER)
        );

        templateContent.put(CTSC_CONTACT_DETAILS, CtscContactDetails
            .builder()
            .emailAddress(email)
            .phoneNumber(phoneNumber)
            .build());

        return templateContent;
    }
}
