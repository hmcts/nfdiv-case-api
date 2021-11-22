package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class NoticeOfProceedingContent {

    public static final String DUE_DATE = "dueDate";
    public static final String SUBMISSION_RESPONSE_DATE = "submissionResponseDate";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL = "divorceOrCivilPartnershipEmail";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS = "divorceOrCivilPartnershipProceedings";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP = "divorceOrEndCivilPartnership";
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
    public static final String CONTACT_DIVORCE_JUSTICE_GOV_UK = "contactdivorce@justice.gov.uk";
    public static final String DIVORCE_PROCEEDINGS = "divorce proceedings";
    public static final String DIVORCE_APPLICATION = "divorce application";
    public static final String DIVORCE_PROCESS = "divorce process";
    public static final String YOUR_DIVORCE = "your divorce";
    public static final String DIVORCE = "divorce";
    public static final String DIVORCE_URL = "https://www.gov.uk/divorce";
    public static final String DIVORCE_SERVICE = "Divorce service";
    public static final String THE_DIVORCE_SERVICE = "The Divorce service";
    public static final String CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK = "civilpartnership.case@justice.gov.uk";
    public static final String PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP = "proceedings to end your civil partnership";
    public static final String TO_END_YOUR_CIVIL_PARTNERSHIP = "to end your civil partnership";
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

    private static final int HOLDING_DUE_DATE_OFFSET_DAYS = 141;
    private static final int PAPER_SERVE_OFFSET_DAYS = 28;

    @Autowired
    private CommonContent commonContent;

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

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(CASE_REFERENCE, ccdCaseReference);
        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(DUE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(
            SUBMISSION_RESPONSE_DATE,
            caseData.getApplication().getIssueDate().plusDays(HOLDING_DUE_DATE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
        );

        templateContent.put(
            SERVE_PAPERS_BEFORE_DATE,
            caseData.getApplication().getIssueDate().plusDays(PAPER_SERVE_OFFSET_DAYS).format(DATE_TIME_FORMATTER)
        );

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
            templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE);
            templateContent.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS);

        } else {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_YOUR_CIVIL_PARTNERSHIP);
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
        }
        final var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();

        templateContent.put("ctscContactDetails", ctscContactDetails);

        return templateContent;
    }
}
