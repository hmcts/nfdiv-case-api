package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_PROCEEDINGS_CY;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.TO_END_YOUR_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class NoticeOfProceedingJointContent {

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
    public static final String END_A_CIVIL_PARTNERSHIP_SERVICE = "End A Civil Partnership Service";
    public static final String DIVORCE_PROCEEDINGS = "divorce proceedings";
    public static final String DIVORCE_APPLICATION = "divorce application";
    public static final String DIVORCE_PROCESS = "divorce process";
    public static final String DIVORCE_PROCESS_CY = "broses ysgaru";
    public static final String YOUR_DIVORCE = "your divorce";
    public static final String DIVORCE = "divorce";
    public static final String DIVORCE_URL = "https://www.gov.uk/divorce";
    public static final String DIVORCE_SERVICE = "Divorce Service";
    public static final String THE_DIVORCE_SERVICE = "The Divorce Service";
    public static final String PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP = "proceedings to end your civil partnership";
    public static final String TO_END_YOUR_CIVIL_PARTNERSHIP = "to end your civil partnership";
    public static final String APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP = "application to end your civil partnership";
    public static final String PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP = "process to end your civil partnership";
    public static final String PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY = "proses i ddod â’ch partneriaeth sifil i ben";
    public static final String YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP = "your application to end your civil partnership";
    public static final String ENDING_YOUR_CIVIL_PARTNERSHIP = "ending your civil partnership";
    public static final String ENDING_A_CIVIL_PARTNERSHIP = "ending a civil partnership";
    public static final String CIVIL_PARTNERSHIP_EMAIL = "https://www.gov.uk/end-civil-partnership";
    public static final String DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP = "divorceOrEndYourCivilPartnership";
    public static final String MARRIAGE_OR_CIVIL_PARTNER = "marriageOrCivilPartner";
    public static final String MARRIAGE = "marriage";
    public static final String CIVIL_PARTNERSHIP = "civil partnership";
    public static final String DISPLAY_EMAIL_CONFIRMATION = "displayEmailConfirmation";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String ADDRESS = "address";

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     Applicant applicant,
                                     Applicant partner) {

        final LanguagePreference applicantLanguagePreference = applicant.getLanguagePreference();
        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicantLanguagePreference);

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());

        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());

        templateContent.put(RELATION, commonContent.getPartner(caseData, partner, applicantLanguagePreference));

        boolean displayEmailConfirmation = !applicant.isApplicantOffline() && ObjectUtils.isNotEmpty(applicant.getEmail());
        templateContent.put(DISPLAY_EMAIL_CONFIRMATION, displayEmailConfirmation);

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));

        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(DUE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(
            SUBMISSION_RESPONSE_DATE,
            holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()).format(DATE_TIME_FORMATTER)
        );

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS,
                WELSH.equals(applicantLanguagePreference) ? DIVORCE_PROCEEDINGS_CY : DIVORCE_PROCEEDINGS);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP,
                WELSH.equals(applicantLanguagePreference) ? FOR_A_DIVORCE_CY : FOR_A_DIVORCE);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION,
                WELSH.equals(applicantLanguagePreference) ? DIVORCE_APPLICATION_CY : DIVORCE_APPLICATION);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS,
                WELSH.equals(applicantLanguagePreference)
                    ? DIVORCE_PROCESS_CY
                    : DIVORCE_PROCESS
            );
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE);
            templateContent.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
        } else {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS,
                WELSH.equals(applicantLanguagePreference)
                    ? PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY
                    : PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP
            );
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP,
                WELSH.equals(applicantLanguagePreference) ? TO_END_YOUR_CIVIL_PARTNERSHIP_CY : TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION,
                WELSH.equals(applicantLanguagePreference)
                    ? APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP_CY
                    : APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP
            );
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS,
                WELSH.equals(applicantLanguagePreference)
                    ? PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP_CY
                    : PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP
            );
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, ENDING_A_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, ENDING_YOUR_CIVIL_PARTNERSHIP);

            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, CIVIL_PARTNERSHIP_EMAIL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, END_A_CIVIL_PARTNERSHIP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_A_CIVIL_PARTNERSHIP_SERVICE);

            templateContent.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNER, CIVIL_PARTNERSHIP);
        }

        return templateContent;
    }
}
