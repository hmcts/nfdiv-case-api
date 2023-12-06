package uk.gov.hmcts.divorce.document.content.templatecontent;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.common.exception.InvalidCcdCaseDataException;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A1_SOLE_APP1_CIT_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AL2_SOLE_APP1_CIT_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_OS_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP2_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R1_SOLE_APP2_CIT_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_OUTSIDE_ENGLAND_WALES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.A_DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPLAY_HEADER_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ENDING_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_OFFLINE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_REGISTERED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPOND_BY_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WHO_APPLIED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class NoticeOfProceedingSoleTemplateContent implements TemplateContent {

    public static final String SUBMISSION_RESPONSE_DATE = "submissionResponseDate";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS = "divorceOrCivilPartnershipProceedings";
    public static final String DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP = "divorceOrEndTheirCivilPartnership";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION = "divorceOrEndCivilPartnershipApplication";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS = "divorceOrEndCivilPartnershipProcess";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION = "divorceOrCivilPartnershipApplication";
    public static final String DIVORCE_OR_END_A_CIVIL_PARTNERSHIP = "divorceOrEndACivilPartnership";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_URL = "divorceOrCivilPartnershipUrl";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE = "divorceOrCivilPartnershipService";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS = "divorceOrCivilPartnershipPapers";
    public static final String DIVORCE_PROCEEDINGS = "divorce proceedings";
    public static final String DIVORCE_PROCESS_CY = "broses ysgaru";
    public static final String YOUR_DIVORCE = "your divorce";
    public static final String YOUR_DIVORCE_CY = "eich ysgariad";
    public static final String DIVORCE_CY = "ysgariadau";
    public static final String DIVORCE_URL = "https://www.gov.uk/divorce";
    public static final String DIVORCE_SERVICE = "Divorce Service";
    public static final String THE_DIVORCE_SERVICE = "The Divorce Service";
    public static final String PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP = "proceedings to end your civil partnership";
    public static final String PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY = "gais i ddod â’ch partneriaeth sifil i ben";
    public static final String TO_END_YOUR_CIVIL_PARTNERSHIP = "to end your civil partnership";
    public static final String TO_END_YOUR_CIVIL_PARTNERSHIP_CY = "i ddod â’ch partneriaeth sifil i ben";
    public static final String TO_END_THEIR_CIVIL_PARTNERSHIP = "to end their civil partnership";
    public static final String APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP_CY = "cais i ddod â’ch partneriaeth sifil i ben";
    public static final String PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY = "proses i ddod â’ch partneriaeth sifil i ben";
    public static final String YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP = "your application to end your civil partnership";
    public static final String YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP_CY = "eich cais i ddod â’ch partneriaeth sifil i ben";
    public static final String ENDING_YOUR_CIVIL_PARTNERSHIP = "ending your civil partnership";
    public static final String ENDING_A_CIVIL_PARTNERSHIP = "ending a civil partnership";
    public static final String CIVIL_PARTNERSHIP_EMAIL = "https://www.gov.uk/end-civil-partnership";
    public static final String DIVORCE_PAPERS = "divorce papers";
    public static final String PAPERS_TO_END_YOUR_CIVIL_PARTNERSHIP = "papers to end your civil partnership";
    public static final String SERVE_PAPERS_BEFORE_DATE = "servePapersBeforeDate";
    public static final String DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP = "divorceOrEndYourCivilPartnership";
    public static final String BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP = "beenMarriedOrEnteredIntoCivilPartnership";
    public static final String ENTERED_INTO_A_CIVIL_PARTNERSHIP_WITH = "entered into a civil partnership with";
    public static final String BEEN_MARRIED_TO = "been married to";
    public static final String MARRIAGE_OR_CIVIL_PARTNER = "marriageOrCivilPartner";
    public static final String APPLICANT_2_ADDRESS = "applicant2Address";
    public static final String APPLICANT_2_IS_REPRESENTED = "applicant2IsRepresented";
    public static final String DISPLAY_EMAIL_CONFIRMATION = "displayEmailConfirmation";
    public static final String HAS_CASE_BEEN_REISSUED = "hasCaseBeenReissued";
    public static final String REISSUE_DATE = "reissueDate";
    public static final String IS_COURT_SERVICE = "isCourtService";
    public static final String IS_PERSONAL_SERVICE = "isPersonalService";
    public static final String URL_TO_LINK_CASE = "linkCaseUrl";
    public static final String RELATIONS_SOLICITOR = "relationsSolicitor";
    public static final String IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE = "isRespondentSolicitorPersonalService";
    public static final String IS_RESPONDENT_BASED_IN_UK = "isRespondentBasedInUk";
    public static final String CAN_SERVE_BY_EMAIL = "canServeByEmail";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_DOCUMENTS = "divorceOrCivilPartnershipDocuments";
    public static final String DIVORCE_DOCUMENTS = "divorce documents";
    public static final String CIVIL_PARTNERSHIP_DOCUMENTS = "documents to end your civil partnership";

    private static final int PAPER_SERVE_OFFSET_DAYS = 28;
    public static final int RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS = 16;

    private static final String RESPONDENT_SIGN_IN_DIVORCE_URL = "respondentSignInDivorceUrl";
    private static final String RESPONDENT_SIGN_IN_DISSOLUTION_URL = "respondentSignInDissolutionUrl";

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisTemplatesConfig config;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;


    @Override
    public List<String> getSupportedTemplates() {
        return List.of(NFD_NOP_A1_SOLE_APP1_CIT_CS, NFD_NOP_AL2_SOLE_APP1_CIT_PS, NFD_NOP_APP1_JS_SOLE,
                NFD_NOP_APP1_JS_SOLE_OS_PS, NFD_NOP_APP2_JS_SOLE, NFD_NOP_R2_SOLE_APP2_OUTSIDE_ENGLAND_WALES,
                NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE, NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE, NFD_NOP_R1_SOLE_APP2_CIT_ONLINE);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {

        var languagePreference = applicant.getLanguagePreference();
        var partner = caseData.getApplicant2();
        var application = caseData.getApplication();

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(languagePreference);

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(APPLICANT_1_FIRST_NAME, applicant.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant.getLastName());
        templateContent.put(APPLICANT_2_FIRST_NAME, partner.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, partner.getLastName());
        templateContent.put(APPLICANT_1_FULL_NAME, applicant.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, partner.getFullName());

        if (application.getIssueDate() == null) {
            throw new InvalidCcdCaseDataException("Cannot generate notice of proceeding without issue date. Case ID: " + caseId);
        }

        templateContent.put(ISSUE_DATE, application.getIssueDate().format(DATE_TIME_FORMATTER));
        if (!isNull(caseData.getDueDate())) {
            templateContent.put(DUE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        }

        templateContent.put(IS_OFFLINE, applicant.isApplicantOffline());

        templateContent.put(
                SUBMISSION_RESPONSE_DATE,
                holdingPeriodService.getDueDateFor(application.getIssueDate()).format(DATE_TIME_FORMATTER)
        );

        templateContent.put(
                SERVE_PAPERS_BEFORE_DATE,
                application.getIssueDate().plusDays(PAPER_SERVE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
        );

        templateContent.put(APPLICANT_1_ADDRESS, applicant.getPostalAddress());
        templateContent.put(APPLICANT_2_ADDRESS, partner.getPostalAddress());

        templateContent.put(
                APPLICANT_1_SOLICITOR_NAME,
                applicant.isRepresented()
                        ? applicant.getSolicitor().getName()
                        : NOT_REPRESENTED);

        boolean displayEmailConfirmation = !applicant.isApplicantOffline()
                && ObjectUtils.isNotEmpty(applicant.getEmail());

        templateContent.put(DISPLAY_EMAIL_CONFIRMATION, displayEmailConfirmation);

        final boolean personalServiceMethod = PERSONAL_SERVICE.equals(application.getServiceMethod());
        final boolean isApplicant2Represented = partner.isRepresented();
        templateContent.put(IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE, personalServiceMethod && isApplicant2Represented);

        if (partner.isRepresented()) {
            generateSoleRespondentRepresentedContent(templateContent, caseData, personalServiceMethod, languagePreference);
        }

        if (!isNull(application.getReissueDate())) {
            templateContent.put(HAS_CASE_BEEN_REISSUED, true);
            templateContent.put(REISSUE_DATE, application.getReissueDate().format(DATE_TIME_FORMATTER));
            templateContent.put(
                    RESPOND_BY_DATE,
                    application.getReissueDate().plusDays(RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
            );
        } else {
            templateContent.put(
                    RESPOND_BY_DATE,
                    application.getIssueDate().plusDays(RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
            );
        }

        templateContent.put(IS_RESPONDENT_BASED_IN_UK, !partner.isBasedOverseas());
        templateContent.put(CAN_SERVE_BY_EMAIL,
                !applicant.isApplicantOffline() && !partner.isBasedOverseas());

        templateContent.put(IS_COURT_SERVICE, COURT_SERVICE.equals(application.getServiceMethod()));
        templateContent.put(IS_PERSONAL_SERVICE, application.isPersonalServiceMethod());
        templateContent.put(ACCESS_CODE, caseData.getCaseInvite().accessCode());
        templateContent.put(URL_TO_LINK_CASE,
                config.getTemplateVars().get(caseData.isDivorce() ? RESPONDENT_SIGN_IN_DIVORCE_URL : RESPONDENT_SIGN_IN_DISSOLUTION_URL));

        generateDivorceOrDissolutionContent(templateContent, caseData, partner, applicant.getLanguagePreference());

        if (!application.isCourtServiceMethod()) {
            templateContent.put(DISPLAY_HEADER_ADDRESS, true);
        }

        return templateContent;
    }

    private void generateDivorceOrDissolutionContent(Map<String, Object> templateContent,
                                                     CaseData caseData,
                                                     Applicant partner,
                                                     LanguagePreference languagePreference) {

        templateContent.put(RELATION, commonContent.getPartner(caseData, partner, languagePreference));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(CommonContent.IS_DIVORCE, true);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS,
                WELSH.equals(languagePreference) ? A_DIVORCE_APPLICATION_CY : DIVORCE_PROCEEDINGS);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? FOR_A_DIVORCE_CY : FOR_A_DIVORCE);
            templateContent.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION,
                WELSH.equals(languagePreference) ? DIVORCE_APPLICATION_CY : DIVORCE_APPLICATION);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS,
                WELSH.equals(languagePreference) ? DIVORCE_PROCESS_CY : DIVORCE_PROCESS);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION,
                WELSH.equals(languagePreference) ? YOUR_DIVORCE_CY : YOUR_DIVORCE);
            templateContent.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? DIVORCE_CY : DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS);
            templateContent.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, BEEN_MARRIED_TO);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_DOCUMENTS, DIVORCE_DOCUMENTS);
        } else {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(CommonContent.IS_DIVORCE, false);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS,
                WELSH.equals(languagePreference)
                    ? PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY
                    : PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? TO_END_YOUR_CIVIL_PARTNERSHIP_CY : TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, TO_END_THEIR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION,
                WELSH.equals(languagePreference)
                    ? APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP_CY
                    : APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS,
                WELSH.equals(languagePreference) ? PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY : APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION,
                WELSH.equals(languagePreference)
                    ? YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP_CY
                    : YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, ENDING_A_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? ENDING_CIVIL_PARTNERSHIP_CY : ENDING_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, CIVIL_PARTNERSHIP_EMAIL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, END_CP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_CP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, PAPERS_TO_END_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);
            templateContent.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, ENTERED_INTO_A_CIVIL_PARTNERSHIP_WITH);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNER, CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_DOCUMENTS, CIVIL_PARTNERSHIP_DOCUMENTS);
        }
    }

    private void generateSoleRespondentRepresentedContent(Map<String, Object> templateContent,
                                                          CaseData caseData,
                                                          boolean personalServiceMethod,
                                                          LanguagePreference languagePreference) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();
        final Solicitor applicant1Solicitor = applicant1.getSolicitor();
        final Solicitor applicant2Solicitor = caseData.getApplicant2().getSolicitor();

        templateContent.put(SOLICITOR_NAME, applicant2Solicitor.getName());
        templateContent.put(SOLICITOR_ADDRESS, applicant2Solicitor.getAddress());

        templateContent.put(
            SOLICITOR_REFERENCE,
            !isNull(applicant1Solicitor) && isNotEmpty(applicant1Solicitor.getReference())
                ? applicant1Solicitor.getReference()
                : NOT_PROVIDED
        );

        templateContent.put(
            SOLICITOR_NAME_WITH_DEFAULT_VALUE,
            !isNull(applicant1Solicitor) && applicant1.isRepresented() ? applicant1Solicitor.getName() : NOT_REPRESENTED
        );

        templateContent.put(WHO_APPLIED, applicant1.isRepresented() ? "applicant's solicitor" : "applicant");

        templateContent.put(RESPONDENT_SOLICITOR_REGISTERED, applicant2Solicitor.hasOrgId() ? "Yes" : "No");
        templateContent.put(APPLICANT_2_IS_REPRESENTED, caseData.getApplicant2().isRepresented());

        if (personalServiceMethod) {
            if (WELSH.equals(languagePreference)) {
                templateContent.put(RELATIONS_SOLICITOR,
                    "cyfreithiwr eich " + commonContent.getPartner(caseData, applicant2, languagePreference));
            } else {
                final String relationsSolicitorSuffix = caseData.getDivorceOrDissolution().isDivorce() ? "'s solicitor" : "s' solicitor";
                templateContent.put(RELATIONS_SOLICITOR, commonContent.getPartner(caseData, applicant2) + relationsSolicitorSuffix);
            }
        }
    }
}
