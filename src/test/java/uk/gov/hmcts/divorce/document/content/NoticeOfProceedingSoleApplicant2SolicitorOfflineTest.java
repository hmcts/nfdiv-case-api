package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class NoticeOfProceedingSoleApplicant2SolicitorOfflineTest {

    private static final String ADDRESS = "line 1\ntown\npostcode";
    private static final LocalDate APPLICATION_ISSUE_DATE = LocalDate.of(2022, 3, 30);
    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
        .builder()
        .emailAddress("divorcecase@justice.gov.uk")
        .phoneNumber("0300 303 0642")
        .build();

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private NoticeOfProceedingSoleApplicant2SolicitorOffline noticeOfProceedingSoleApplicant2SolicitorOffline;

    @BeforeEach
    public void setUp() {
        setField(noticeOfProceedingSoleApplicant2SolicitorOffline, "email", "divorcecase@justice.gov.uk");
        setField(noticeOfProceedingSoleApplicant2SolicitorOffline, "phoneNumber", "0300 303 0642");
    }

    @Test
    public void shouldMapTemplateContentForSoleDivorceApplication() {
        Applicant applicant2 = applicantRepresentedBySolicitor();
        applicant2.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant2.getSolicitor().setAddress(ADDRESS);
        applicant2.getSolicitor().setReference("12345");

        CaseData caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .applicant2(applicant2)
            .divorceOrDissolution(DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .build())
            .build();

        when(holdingPeriodService.getRespondByDateFor(APPLICATION_ISSUE_DATE))
            .thenReturn(APPLICATION_ISSUE_DATE.plusDays(16));

        final Map<String, Object> templateContent = noticeOfProceedingSoleApplicant2SolicitorOffline.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "30 March 2022"),
            entry(DUE_DATE, "15 April 2022"),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
            entry(APPLICANT_1_LAST_NAME, "test_last_name"),
            entry(APPLICANT_2_FIRST_NAME, "test_first_name"),
            entry(APPLICANT_2_LAST_NAME, "test_last_name"),
            entry(SOLICITOR_NAME, "The Solicitor"),
            entry(SOLICITOR_ADDRESS, ADDRESS),
            entry(SOLICITOR_REFERENCE, "12345"),
            entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
            entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
            entry(IS_JOINT, false),
            entry(IS_DIVORCE, true),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry("isApp1Represented", true)
        );
    }

    @Test
    public void shouldMapTemplateContentForSoleDissolutionApplication() {
        Applicant applicant2 = applicantRepresentedBySolicitor();
        applicant2.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant2.getSolicitor().setAddress(ADDRESS);
        applicant2.getSolicitor().setReference("12345");

        CaseData caseData = CaseData.builder()
            .applicant1(applicantRepresentedBySolicitor())
            .applicant2(applicant2)
            .divorceOrDissolution(DISSOLUTION)
            .applicationType(SOLE_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .build())
            .build();

        when(holdingPeriodService.getRespondByDateFor(APPLICATION_ISSUE_DATE))
            .thenReturn(APPLICATION_ISSUE_DATE.plusDays(16));

        final Map<String, Object> templateContent = noticeOfProceedingSoleApplicant2SolicitorOffline.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "30 March 2022"),
            entry(DUE_DATE, "15 April 2022"),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
            entry(APPLICANT_1_LAST_NAME, "test_last_name"),
            entry(APPLICANT_2_FIRST_NAME, "test_first_name"),
            entry(APPLICANT_2_LAST_NAME, "test_last_name"),
            entry(SOLICITOR_NAME, "The Solicitor"),
            entry(SOLICITOR_ADDRESS, ADDRESS),
            entry(SOLICITOR_REFERENCE, "12345"),
            entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
            entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
            entry(IS_JOINT, false),
            entry(IS_DIVORCE, false),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry("isApp1Represented", true)
        );
    }

    @Test
    public void shouldMapTemplateContentForSoleDissolutionApplicationApplicant1NotRepresented() {
        Applicant applicant2 = applicantRepresentedBySolicitor();
        applicant2.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant2.getSolicitor().setAddress(ADDRESS);
        applicant2.getSolicitor().setReference("12345");

        CaseData caseData = CaseData.builder()
            .applicant1(getApplicant(FEMALE))
            .applicant2(applicant2)
            .divorceOrDissolution(DISSOLUTION)
            .applicationType(SOLE_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .build())
            .build();

        when(holdingPeriodService.getRespondByDateFor(APPLICATION_ISSUE_DATE))
            .thenReturn(APPLICATION_ISSUE_DATE.plusDays(16));

        final Map<String, Object> templateContent = noticeOfProceedingSoleApplicant2SolicitorOffline.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "30 March 2022"),
            entry(DUE_DATE, "15 April 2022"),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
            entry(APPLICANT_1_LAST_NAME, "test_last_name"),
            entry(APPLICANT_2_FIRST_NAME, "test_first_name"),
            entry(APPLICANT_2_LAST_NAME, "test_last_name"),
            entry(SOLICITOR_NAME, "The Solicitor"),
            entry(SOLICITOR_ADDRESS, ADDRESS),
            entry(SOLICITOR_REFERENCE, "12345"),
            entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
            entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
            entry(IS_JOINT, false),
            entry(IS_DIVORCE, false),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry("isApp1Represented", false)
        );
    }
}