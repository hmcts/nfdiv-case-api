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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_REGISTERED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class NoticeOfProceedingSolicitorContent {

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference, boolean isApplicantSolicitor) {

        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        Applicant applicant1 = caseData.getApplicant1();
        Applicant applicant2 = caseData.getApplicant2();
        Solicitor applicant1Solicitor = applicant1.getSolicitor();
        Solicitor applicant2Solicitor = applicant2.getSolicitor();
        LocalDate applicationIssueDate = caseData.getApplication().getIssueDate();
        boolean isJoint = !caseData.getApplicationType().isSole();
        boolean oneSolicitorApplyingForBothParties = applicant1.isRepresented() && applicant2.isRepresented()
            && applicant1Solicitor.equals(applicant2Solicitor);

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
        templateContent.put(ISSUE_DATE, applicationIssueDate.format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, isJoint);
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(APPLICANT_SOLICITOR_LABEL,
            isJoint && oneSolicitorApplyingForBothParties ? "Applicants solicitor" : "Applicant's solicitor");
        templateContent.put(APPLICANT_SOLICITOR_REGISTERED, applicant1Solicitor.hasOrgId());
        templateContent.put(SOLICITOR_NAME, isApplicantSolicitor ? applicant1Solicitor.getName() : applicant2Solicitor.getName());
        templateContent.put(SOLICITOR_ADDRESS, isApplicantSolicitor ? applicant1Solicitor.getAddress() : applicant2Solicitor.getAddress());

        templateContent.put(
            SOLICITOR_REFERENCE,
            isApplicantSolicitor ? solicitorReference(applicant1Solicitor) : solicitorReference(applicant2Solicitor)
        );

        templateContent.put(
            SOLICITOR_NAME_WITH_DEFAULT_VALUE,
            isApplicantSolicitor ? solicitorName(applicant1, applicant1Solicitor) : solicitorName(applicant2, applicant2Solicitor)
        );

        if (!isJoint) {
            templateContent.put(
                DUE_DATE,
                holdingPeriodService.getRespondByDateFor(applicationIssueDate).format(DATE_TIME_FORMATTER)
            );
        }

        templateContent.put(CTSC_CONTACT_DETAILS, CtscContactDetails
            .builder()
            .emailAddress(email)
            .phoneNumber(phoneNumber)
            .build());

        return templateContent;
    }

    private String solicitorName(Applicant applicant, Solicitor solicitor) {
        return applicant.isRepresented() ? solicitor.getName() : NOT_REPRESENTED;
    }

    private String solicitorReference(Solicitor solicitor) {
        return isNotEmpty(solicitor.getReference()) ? solicitor.getReference() : NOT_PROVIDED;
    }
}
