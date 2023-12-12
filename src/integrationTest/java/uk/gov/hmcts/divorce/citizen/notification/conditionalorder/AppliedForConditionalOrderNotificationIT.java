package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification.APPLICANT1;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification.APPLICANT2;
import static uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification.PLUS_14_DUE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@SpringBootTest
public class AppliedForConditionalOrderNotificationIT {

    @Autowired
    private AppliedForConditionalOrderNotification appliedForConditionalOrderNotification;

    private static final LocalDate date = LocalDate.of(2021, 11, 10);
    private static final LocalDateTime submittedDate = LocalDateTime.of(2021, 11, 10, 0, 0, 0);

    @Test
    public void shouldPopulateApplicant1TemplateContent() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName("Bob");
        caseData.getApplicant1().setLastName("Smith");
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(submittedDate);

        Map<String, String> templateContent = appliedForConditionalOrderNotification.partnerTemplateVars(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2(),
            APPLICANT1);

        assertThat(templateContent).contains(
            entry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(IS_DIVORCE, YES),
            entry(IS_DISSOLUTION, NO),
            entry(FIRST_NAME, "Bob"),
            entry(LAST_NAME, "Smith"),
            entry(PARTNER, "wife"),
            entry(COURT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PLUS_14_DUE_DATE, submittedDate.plusDays(14).format(DATE_TIME_FORMATTER)),
            entry(SIGN_IN_URL, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net")
        );
    }

    @Test
    public void shouldPopulateApplicant1WelshTemplateContent() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName("Bob");
        caseData.getApplicant1().setLastName("Smith");
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(submittedDate);

        Map<String, String> templateContent = appliedForConditionalOrderNotification.partnerTemplateVars(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2(),
            APPLICANT1);

        assertThat(templateContent).contains(
            entry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(IS_DIVORCE, YES),
            entry(IS_DISSOLUTION, NO),
            entry(FIRST_NAME, "Bob"),
            entry(LAST_NAME, "Smith"),
            entry(PARTNER, "gwraig"),
            entry(COURT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PLUS_14_DUE_DATE, submittedDate.plusDays(14).format(DATE_TIME_FORMATTER)),
            entry(SIGN_IN_URL, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net")
        );
    }

    @Test
    public void shouldPopulateApplicant1SolicitorTemplateContent() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName("Bob");
        caseData.getApplicant1().setLastName("Smith");
        caseData.getApplicant2().setFirstName("Jane");
        caseData.getApplicant2().setLastName("Smith");
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(submittedDate);
        caseData.getApplication().setIssueDate(date);
        caseData.setDueDate(date);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name(TEST_SOLICITOR_NAME)
                .email(TEST_SOLICITOR_EMAIL)
                .reference("REF01")
                .build()
        );

        Map<String, String> templateContent = appliedForConditionalOrderNotification.solicitorTemplateVars(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1().getSolicitor());

        assertThat(templateContent).contains(
            entry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(APPLICANT_NAME, "Bob Smith"),
            entry(RESPONDENT_NAME, "Jane Smith"),
            entry(ISSUE_DATE, date.format(DATE_TIME_FORMATTER)),
            entry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            entry(SOLICITOR_REFERENCE, "REF01"),
            entry(COURT_EMAIL, CONTACT_DIVORCE_EMAIL)
        );
    }

    @Test
    public void shouldPopulateApplicant2TemplateContent() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setFirstName("Bob");
        caseData.getApplicant2().setLastName("Smith");
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().setSubmittedDate(submittedDate);

        Map<String, String> templateContent = appliedForConditionalOrderNotification.partnerTemplateVars(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            caseData.getApplicant1(),
            APPLICANT2);

        assertThat(templateContent).contains(
            entry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(IS_DIVORCE, YES),
            entry(IS_DISSOLUTION, NO),
            entry(FIRST_NAME, "Bob"),
            entry(LAST_NAME, "Smith"),
            entry(PARTNER, "wife"),
            entry(COURT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PLUS_14_DUE_DATE, submittedDate.plusDays(14).format(DATE_TIME_FORMATTER)),
            entry(SIGN_IN_URL, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net")
        );
    }

    @Test
    public void shouldPopulateApplicant2WelshTemplateContent() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setFirstName("Bob");
        caseData.getApplicant2().setLastName("Smith");
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().setSubmittedDate(submittedDate);

        Map<String, String> templateContent = appliedForConditionalOrderNotification.partnerTemplateVars(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            caseData.getApplicant1(),
            APPLICANT2
        );

        assertThat(templateContent).contains(
            entry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(IS_DIVORCE, YES),
            entry(IS_DISSOLUTION, NO),
            entry(FIRST_NAME, "Bob"),
            entry(LAST_NAME, "Smith"),
            entry(PARTNER, "gwraig"),
            entry(COURT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PLUS_14_DUE_DATE, submittedDate.plusDays(14).format(DATE_TIME_FORMATTER)),
            entry(SIGN_IN_URL, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net")
        );
    }
}
