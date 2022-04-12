package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_REGISTERED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPOND_BY_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WHO_APPLIED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class NoticeOfProceedingContent {

    public static final String DUE_DATE = "dueDate";
    public static final String SUBMISSION_RESPONSE_DATE = "submissionResponseDate";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL = "divorceOrCivilPartnershipEmail";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS = "divorceOrCivilPartnershipProceedings";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP = "divorceOrEndCivilPartnership";
    public static final String DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP = "divorceOrEndTheirCivilPartnership";
    public static final String RELATION = "relation";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION = "divorceOrEndCivilPartnershipApplication";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS = "divorceOrEndCivilPartnershipProcess";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION = "divorceOrCivilPartnershipApplication";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP = "divorceOrCivilPartnership";
    public static final String DIVORCE_OR_END_A_CIVIL_PARTNERSHIP = "divorceOrEndACivilPartnership";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_URL = "divorceOrCivilPartnershipUrl";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE = "divorceOrCivilPartnershipService";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER = "divorceOrCivilPartnershipServiceHeader";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS = "divorceOrCivilPartnershipPapers";
    public static final String END_A_CIVIL_PARTNERSHIP_SERVICE = "End A Civil Partnership Service";
    public static final String DIVORCE_PROCEEDINGS = "divorce proceedings";
    public static final String DIVORCE_APPLICATION = "divorce application";
    public static final String DIVORCE_PROCESS = "divorce process";
    public static final String YOUR_DIVORCE = "your divorce";
    public static final String DIVORCE = "divorce";
    public static final String DIVORCE_URL = "https://www.gov.uk/divorce";
    public static final String DIVORCE_SERVICE = "Divorce Service";
    public static final String THE_DIVORCE_SERVICE = "The Divorce Service";
    public static final String PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP = "proceedings to end your civil partnership";
    public static final String TO_END_YOUR_CIVIL_PARTNERSHIP = "to end your civil partnership";
    public static final String TO_END_THEIR_CIVIL_PARTNERSHIP = "to end their civil partnership";
    public static final String CIVIL_PARTNER = "civil partner";
    public static final String APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP = "application to end your civil partnership";
    public static final String PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP = "process to end your civil partnership";
    public static final String YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP = "your application to end your civil partnership";
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
    public static final String MARRIAGE = "marriage";
    public static final String CIVIL_PARTNERSHIP = "civil partnership";
    public static final String APPLICANT_1_ADDRESS = "applicant1Address";
    public static final String APPLICANT_2_ADDRESS = "applicant2Address";
    public static final String APPLICANT_1_SOLICITOR_NAME = "applicant1SolicitorName";
    public static final String DISPLAY_EMAIL_CONFIRMATION = "displayEmailConfirmation";
    public static final String HAS_CASE_BEEN_REISSUED = "hasCaseBeenReissued";
    public static final String REISSUE_DATE = "reissueDate";
    public static final String IS_COURT_SERVICE = "isCourtService";
    public static final String ACCESS_CODE = "accessCode";
    public static final String URL_TO_LINK_CASE = "linkCaseUrl";
    private static final int PAPER_SERVE_OFFSET_DAYS = 28;
    private static final int RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS = 16;

    private static final String APPLICANT_2_SIGN_IN_DIVORCE_URL = "applicant2SignInDivorceUrl";
    private static final String APPLICANT_2_SIGN_IN_DISSOLUTION_URL = "applicant2SignInDissolutionUrl";

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisTemplatesConfig config;

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

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference, Applicant partner) {

        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        if (!isNull(caseData.getDueDate())) {
            templateContent.put(DUE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        }

        templateContent.put(
            SUBMISSION_RESPONSE_DATE,
            holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()).format(DATE_TIME_FORMATTER)
        );

        templateContent.put(
            SERVE_PAPERS_BEFORE_DATE,
            caseData.getApplication().getIssueDate().plusDays(PAPER_SERVE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
        );

        templateContent.put(APPLICANT_1_ADDRESS, caseData.getApplicant1().getPostalAddress());
        templateContent.put(APPLICANT_2_ADDRESS, caseData.getApplicant2().getPostalAddress());

        templateContent.put(
            APPLICANT_1_SOLICITOR_NAME,
            caseData.getApplicant1().isRepresented()
                ? caseData.getApplicant1().getSolicitor().getName()
                : NOT_REPRESENTED);

        boolean displayEmailConfirmation = !caseData.getApplicant1().isOffline() || caseData.getApplicant1().getEmail() != null;
        templateContent.put(DISPLAY_EMAIL_CONFIRMATION, displayEmailConfirmation);

        if (caseData.getApplicant2().isRepresented()) {
            generateSoleRespondentRepresentedContent(templateContent, caseData);
        }

        if (!isNull(caseData.getApplication().getReissueDate())) {
            templateContent.put(HAS_CASE_BEEN_REISSUED, true);
            templateContent.put(REISSUE_DATE, caseData.getApplication().getReissueDate().format(DATE_TIME_FORMATTER));
            templateContent.put(
                RESPOND_BY_DATE,
                caseData.getApplication().getReissueDate().plusDays(RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
            );
        } else {
            templateContent.put(
                RESPOND_BY_DATE,
                caseData.getApplication().getIssueDate().plusDays(RESPONDENT_SOLICITOR_RESPONSE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
            );
        }

        templateContent.put(IS_COURT_SERVICE, COURT_SERVICE.equals(caseData.getApplication().getServiceMethod()));
        templateContent.put(ACCESS_CODE, caseData.getCaseInvite().accessCode());
        templateContent.put(URL_TO_LINK_CASE,
            config.getTemplateVars().get(caseData.isDivorce() ? APPLICANT_2_SIGN_IN_DIVORCE_URL : APPLICANT_2_SIGN_IN_DISSOLUTION_URL));

        generateDivorceOrDissolutionContent(templateContent, caseData, partner);

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }

    private void generateDivorceOrDissolutionContent(Map<String, Object> templateContent, CaseData caseData, Applicant partner) {
        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
            templateContent.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
            templateContent.put(RELATION, commonContent.getPartner(caseData, partner));
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE);
            templateContent.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS);
            templateContent.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, BEEN_MARRIED_TO);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
        } else {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, TO_END_THEIR_CIVIL_PARTNERSHIP);
            templateContent.put(RELATION, CIVIL_PARTNER);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, ENDING_A_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, ENDING_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, CIVIL_PARTNERSHIP_EMAIL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, END_A_CIVIL_PARTNERSHIP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_A_CIVIL_PARTNERSHIP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, PAPERS_TO_END_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, ENTERED_INTO_A_CIVIL_PARTNERSHIP_WITH);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNER, CIVIL_PARTNERSHIP);
        }
    }

    private void generateSoleRespondentRepresentedContent(Map<String, Object> templateContent, CaseData caseData) {
        final Applicant applicant1 = caseData.getApplicant1();
        final Solicitor applicant1Solicitor = applicant1.getSolicitor();
        final Solicitor applicant2Solicitor = caseData.getApplicant2().getSolicitor();

        templateContent.put(SOLICITOR_NAME, applicant2Solicitor.getName());
        templateContent.put(SOLICITOR_ADDRESS, applicant2Solicitor.getAddress());

        templateContent.put(
            SOLICITOR_REFERENCE,
            isNotEmpty(applicant1Solicitor.getReference()) ? applicant1Solicitor.getReference() : NOT_PROVIDED
        );

        templateContent.put(
                SOLICITOR_NAME_WITH_DEFAULT_VALUE,
            applicant1.isRepresented() ? applicant1Solicitor.getName() : NOT_REPRESENTED
        );

        templateContent.put(WHO_APPLIED, applicant1.isRepresented() ? "applicant's solicitor" : "applicant");

        templateContent.put(RESPONDENT_SOLICITOR_REGISTERED, !isNull(applicant2Solicitor.getOrganisationPolicy()) ? "Yes" : "No");
    }
}
