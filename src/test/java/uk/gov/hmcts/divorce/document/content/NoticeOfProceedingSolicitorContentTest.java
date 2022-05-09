package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.HAS_CASE_BEEN_REISSUED;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.REISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingSolicitorContentTest {

    private static final String ADDRESS = "line 1\ntown\npostcode";
    private static final LocalDate APPLICATION_ISSUE_DATE = LocalDate.of(2022, 3, 30);
    private static final LocalDate APPLICATION_REISSUE_DATE = LocalDate.of(2022, 4, 30);
    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
        .builder()
        .emailAddress("divorcecase@justice.gov.uk")
        .phoneNumber("0300 303 0642")
        .build();

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private NoticeOfProceedingSolicitorContent applicantSolicitorNopContent;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(applicantSolicitorNopContent, "email", "divorcecase@justice.gov.uk");
        ReflectionTestUtils.setField(applicantSolicitorNopContent, "phoneNumber", "0300 303 0642");
    }

    @Test
    public void shouldMapTemplateContentForSoleDivorceApplication() {
        Applicant applicant1 = applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant1.getSolicitor().setAddress(ADDRESS);
        applicant1.getSolicitor().setReference("12345");

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant2(respondent())
            .divorceOrDissolution(DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .build())
            .build();

        when(holdingPeriodService.getRespondByDateFor(APPLICATION_ISSUE_DATE))
            .thenReturn(APPLICATION_ISSUE_DATE.plusDays(16));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent)
            .contains(
                entry(ISSUE_DATE, "30 March 2022"),
                entry(DUE_DATE, "15 April 2022"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
                entry(APPLICANT_1_LAST_NAME, "test_last_name"),
                entry(APPLICANT_2_FIRST_NAME, "applicant_2_first_name"),
                entry(APPLICANT_2_LAST_NAME, "test_last_name"),
                entry(SOLICITOR_NAME, "The Solicitor"),
                entry(SOLICITOR_ADDRESS, ADDRESS),
                entry(SOLICITOR_REFERENCE, "12345"),
                entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
                entry(APPLICANT_SOLICITOR_REGISTERED, true),
                entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
                entry(IS_JOINT, false),
                entry(IS_DIVORCE, true),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT))
            .doesNotContain(
                entry(HAS_CASE_BEEN_REISSUED, true),
                entry(REISSUE_DATE, "30 April 2022"));
    }

    @Test
    public void shouldMapTemplateContentForReissueSoleDivorceApplication() {
        Applicant applicant1 = applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant1.getSolicitor().setAddress(ADDRESS);
        applicant1.getSolicitor().setReference("12345");

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant2(respondent())
            .divorceOrDissolution(DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .reissueDate(APPLICATION_REISSUE_DATE)
                .build())
            .build();

        when(holdingPeriodService.getRespondByDateFor(APPLICATION_ISSUE_DATE))
            .thenReturn(APPLICATION_ISSUE_DATE.plusDays(16));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "30 March 2022"),
            entry(DUE_DATE, "15 April 2022"),
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
            entry(APPLICANT_1_LAST_NAME, "test_last_name"),
            entry(APPLICANT_2_FIRST_NAME, "applicant_2_first_name"),
            entry(APPLICANT_2_LAST_NAME, "test_last_name"),
            entry(SOLICITOR_NAME, "The Solicitor"),
            entry(SOLICITOR_ADDRESS, ADDRESS),
            entry(SOLICITOR_REFERENCE, "12345"),
            entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
            entry(APPLICANT_SOLICITOR_REGISTERED, true),
            entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
            entry(IS_JOINT, false),
            entry(IS_DIVORCE, true),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry(HAS_CASE_BEEN_REISSUED, true),
            entry(REISSUE_DATE, "30 April 2022")
        );
    }

    @Test
    public void shouldMapTemplateContentForJointDivorceApplicationWithOneSolicitorApplyingForBothParties() {
        Applicant applicant1 = applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant1.getSolicitor().setAddress(ADDRESS);

        Applicant applicant2 = respondentWithDigitalSolicitor();
        applicant2.getSolicitor().setAddress(ADDRESS);

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .divorceOrDissolution(DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .build())
            .build();

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent)
            .doesNotContain(
                entry(DUE_DATE, "15 April 2022"))
            .contains(
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
                entry(APPLICANT_1_LAST_NAME, "test_last_name"),
                entry(APPLICANT_2_FIRST_NAME, "applicant_2_first_name"),
                entry(APPLICANT_2_LAST_NAME, "test_last_name"),
                entry(ISSUE_DATE, "30 March 2022"),
                entry(IS_JOINT, true),
                entry(IS_DIVORCE, true),
                entry(APPLICANT_SOLICITOR_LABEL, "Applicants solicitor"),
                entry(APPLICANT_SOLICITOR_REGISTERED, true),
                entry(SOLICITOR_NAME, "The Solicitor"),
                entry(SOLICITOR_ADDRESS, ADDRESS),
                entry(SOLICITOR_REFERENCE, "Not provided"),
                entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT));

        verifyNoInteractions(holdingPeriodService);
    }
}
