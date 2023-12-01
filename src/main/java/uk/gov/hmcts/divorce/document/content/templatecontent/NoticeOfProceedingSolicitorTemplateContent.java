package uk.gov.hmcts.divorce.document.content.templatecontent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1APP2_SOL_JS_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOLICITOR_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS2_SOLE_APP1_SOL_SS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ADDRESS_BASED_OVERSEAS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_REGISTERED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP1_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPOND_BY_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.templatecontent.NoticeOfProceedingSoleTemplateContent.HAS_CASE_BEEN_REISSUED;
import static uk.gov.hmcts.divorce.document.content.templatecontent.NoticeOfProceedingSoleTemplateContent.REISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.templatecontent.NoticeOfProceedingSoleTemplateContent.RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@RequiredArgsConstructor
@Component
@Slf4j
public class NoticeOfProceedingSolicitorTemplateContent implements TemplateContent {

    private final HoldingPeriodService holdingPeriodService;
    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS, NFD_NOP_AS2_SOLE_APP1_SOL_SS, NFD_NOP_APP1APP2_SOL_JS_JOINT,
                NFD_NOP_APP1_SOLICITOR_JS_SOLE, NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant1) {

        var isApplicantSolicitor = applicant1.isRepresented() && caseData.getApplicant1().equals(applicant1);

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
                caseData.getApplicant1().getLanguagePreference());

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());

        Applicant applicant2 = caseData.getApplicant2();
        Solicitor applicant1Solicitor = applicant1.getSolicitor();
        Solicitor applicant2Solicitor = applicant2.getSolicitor();
        LocalDate applicationIssueDate = caseData.getApplication().getIssueDate();
        boolean isJoint = !caseData.getApplicationType().isSole();
        boolean oneSolicitorApplyingForBothParties = applicant1.isRepresented() && applicant2.isRepresented()
                && applicant1Solicitor.equals(applicant2Solicitor);

        templateContent.put(RELATION,
                commonContent.getPartner(
                        caseData,
                        isApplicantSolicitor ? caseData.getApplicant1() : caseData.getApplicant2(),
                        isApplicantSolicitor ? caseData.getApplicant1().getLanguagePreference() : caseData.getApplicant2().getLanguagePreference()
                )
        );

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
        templateContent.put(ISSUE_DATE, applicationIssueDate.format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, isJoint);
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(APPLICANT_SOLICITOR_LABEL,
                isJoint && oneSolicitorApplyingForBothParties ? "Applicants solicitor" : "Applicant's solicitor");
        templateContent.put(APPLICANT_SOLICITOR_REGISTERED,
                isApplicantSolicitor ? applicant1Solicitor.hasOrgId() : applicant2Solicitor.hasOrgId());
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

        templateContent.put(APPLICANT_1_SOLICITOR_NAME, solicitorName(applicant1, applicant1Solicitor));
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, solicitorName(applicant2, applicant2Solicitor));

        if (!isJoint) {
            templateContent.put(
                    DUE_DATE,
                    holdingPeriodService.getRespondByDateFor(applicationIssueDate).format(DATE_TIME_FORMATTER)
            );
        }

        LocalDate reIssueDate = caseData.getApplication().getReissueDate();

        if (nonNull(reIssueDate)) {
            templateContent.put(HAS_CASE_BEEN_REISSUED, true);
            templateContent.put(REISSUE_DATE, reIssueDate.format(DATE_TIME_FORMATTER));
            templateContent.put(
                    RESPOND_BY_DATE,
                    reIssueDate.plusDays(RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
            );
        } else {
            templateContent.put(
                    RESPOND_BY_DATE,
                    caseData.getApplication().getIssueDate().plusDays(RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
            );
        }

        if (caseData.isJudicialSeparationCase()) {
            templateContent.put(APPLICANT_2_SOLICITOR_ADDRESS, applicant2Solicitor.getAddress());
            templateContent.put(APPLICANT_1_SOLICITOR_ADDRESS, applicant1Solicitor.getAddress());
            templateContent.put(IS_APP1_REPRESENTED, applicant1.isRepresented());
            templateContent.put(ADDRESS_BASED_OVERSEAS, !AddressUtil.isEnglandOrWales(applicant2.getAddress()));
        }

        return templateContent;
    }

    private String solicitorName(Applicant applicant, Solicitor solicitor) {
        return applicant.isRepresented() ? solicitor.getName() : NOT_REPRESENTED;
    }

    private String solicitorReference(Solicitor solicitor) {
        return isNotEmpty(solicitor.getReference()) ? solicitor.getReference() : NOT_PROVIDED;
    }
}
