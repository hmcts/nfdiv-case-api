package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.testutil.TestConstants;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ADDRESS_BASED_OVERSEAS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_SOLICITOR_REGISTERED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP1_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.HAS_CASE_BEEN_REISSUED;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.REISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.RELATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingSolicitorContentTest {

    private static final String ADDRESS = "line 1\ntown\npostcode";
    private static final LocalDate APPLICATION_ISSUE_DATE = LocalDate.of(2022, 3, 30);
    private static final LocalDate APPLICATION_REISSUE_DATE = LocalDate.of(2022, 4, 30);

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private NoticeOfProceedingSolicitorContent applicantSolicitorNopContent;

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
        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("husband");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent)
            .contains(
                entry(ISSUE_DATE, "30 March 2022"),
                entry(DUE_DATE, "15 April 2022"),
                entry(RELATION, "husband"),
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
                entry(IS_DIVORCE, true))
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
        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("husband");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent).contains(
            entry(ISSUE_DATE, "30 March 2022"),
            entry(DUE_DATE, "15 April 2022"),
            entry(RELATION, "husband"),
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
            entry(APPLICANT_1_SOLICITOR_NAME, "The Solicitor"),
            entry(IS_JOINT, false),
            entry(IS_DIVORCE, true),
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

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent)
            .doesNotContain(
                entry(DUE_DATE, "15 April 2022"))
            .contains(
                entry(RELATION, "husband"),
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
                entry(APPLICANT_1_SOLICITOR_NAME, "The Solicitor"),
                entry(APPLICANT_2_SOLICITOR_NAME, "The Solicitor"));

        verifyNoInteractions(holdingPeriodService);
    }

    @Test
    public void shouldMapRelationTemplateContentWhenPopulatingForRespondentSolicitor() {
        Applicant applicant1 = applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant1.getSolicitor().setAddress(ADDRESS);
        applicant1.getSolicitor().setReference("12345");

        Applicant applicant2 = applicantRepresentedBySolicitor();
        applicant2.setFirstName(TestConstants.APPLICANT_2_FIRST_NAME);
        applicant2.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant2.getSolicitor().setAddress(ADDRESS);
        applicant2.getSolicitor().setReference("98765");

        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .divorceOrDissolution(DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .build())
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), ENGLISH)).thenReturn("wife");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, false);

        assertThat(templateContent)
            .contains(
                entry(ISSUE_DATE, "30 March 2022"),
                entry(RELATION, "wife"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
                entry(APPLICANT_1_LAST_NAME, "test_last_name"),
                entry(APPLICANT_2_FIRST_NAME, "applicant_2_first_name"),
                entry(APPLICANT_2_LAST_NAME, "test_last_name"),
                entry(SOLICITOR_NAME, "The Solicitor"),
                entry(SOLICITOR_ADDRESS, ADDRESS),
                entry(SOLICITOR_REFERENCE, "98765"),
                entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
                entry(APPLICANT_SOLICITOR_REGISTERED, true),
                entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
                entry(IS_JOINT, true),
                entry(IS_DIVORCE, true));
    }

    @Test
    public void shouldMapTemplateContentForJudicialSeparation() {
        Applicant applicant1 = applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant1.setAddress(AddressGlobalUK.builder()
                .country("UK")
                .build());

        Applicant applicant2 = respondentWithDigitalSolicitor();
        applicant2.getSolicitor().setAddress(ADDRESS);

        applicant2.setAddress(AddressGlobalUK.builder()
                .country("UK")
                .build());

        CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .divorceOrDissolution(DIVORCE)
                .applicationType(JOINT_APPLICATION)
                .application(Application.builder()
                        .issueDate(APPLICATION_ISSUE_DATE)
                        .build())
                .supplementaryCaseType(JUDICIAL_SEPARATION)
                .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
                caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent)
                .doesNotContain(
                        entry(DUE_DATE, "15 April 2022"))
                .contains(
                        entry(RELATION, "husband"),
                        entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                        entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
                        entry(APPLICANT_1_LAST_NAME, "test_last_name"),
                        entry(APPLICANT_2_FIRST_NAME, "applicant_2_first_name"),
                        entry(APPLICANT_2_LAST_NAME, "test_last_name"),
                        entry(ISSUE_DATE, "30 March 2022"),
                        entry(IS_JOINT, true),
                        entry(IS_DIVORCE, true),
                        entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
                        entry(APPLICANT_SOLICITOR_REGISTERED, true),
                        entry(SOLICITOR_NAME, "The Solicitor"),
                        entry(APPLICANT_2_SOLICITOR_ADDRESS, ADDRESS),
                        entry(SOLICITOR_REFERENCE, "Not provided"),
                        entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
                        entry(APPLICANT_1_SOLICITOR_NAME, "The Solicitor"),
                        entry(APPLICANT_2_SOLICITOR_NAME, "The Solicitor"),
                        entry(IS_APP1_REPRESENTED, true));

        verifyNoInteractions(holdingPeriodService);
    }

    @Test
    public void shouldMapTemplateContentForJointDivorceApplicationWithOneSolicitorApplyingForBothPartiesForJudicialSeparation() {
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

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, true);

        assertThat(templateContent)
            .doesNotContain(
                entry(DUE_DATE, "15 April 2022"))
            .contains(
                entry(RELATION, "husband"),
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
                entry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(SOLICITOR_ADDRESS, ADDRESS),
                entry(SOLICITOR_REFERENCE, "Not provided"),
                entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, TEST_SOLICITOR_NAME),
                entry(APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(APPLICANT_2_SOLICITOR_NAME, TEST_SOLICITOR_NAME));

        verifyNoInteractions(holdingPeriodService);
    }

    @Test
    public void shouldMapSolicitorTemplateContentWhenJudicialSeparation() {
        Applicant applicant1 = applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant1.getSolicitor().setAddress(ADDRESS);
        applicant1.getSolicitor().setReference("12345");

        Applicant applicant2 = applicantRepresentedBySolicitor();
        applicant2.setFirstName(TestConstants.APPLICANT_2_FIRST_NAME);
        applicant2.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant2.getSolicitor().setAddress(ADDRESS);
        applicant2.getSolicitor().setReference("98765");
        applicant2.setAddress(AddressGlobalUK.builder().country("UK").build());

        CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .divorceOrDissolution(DIVORCE)
                .applicationType(JOINT_APPLICATION)
                .supplementaryCaseType(JUDICIAL_SEPARATION)
                .application(Application.builder()
                        .issueDate(APPLICATION_ISSUE_DATE)
                        .build())
                .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), ENGLISH)).thenReturn("wife");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
                caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = applicantSolicitorNopContent.apply(caseData, TEST_CASE_ID, false);

        assertThat(templateContent)
                .contains(
                        entry(ISSUE_DATE, "30 March 2022"),
                        entry(RELATION, "wife"),
                        entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                        entry(APPLICANT_1_FIRST_NAME, "test_first_name"),
                        entry(APPLICANT_1_LAST_NAME, "test_last_name"),
                        entry(APPLICANT_2_FIRST_NAME, "applicant_2_first_name"),
                        entry(APPLICANT_2_LAST_NAME, "test_last_name"),
                        entry(SOLICITOR_NAME, "The Solicitor"),
                        entry(SOLICITOR_ADDRESS, ADDRESS),
                        entry(SOLICITOR_REFERENCE, "98765"),
                        entry(APPLICANT_SOLICITOR_LABEL, "Applicant's solicitor"),
                        entry(APPLICANT_SOLICITOR_REGISTERED, true),
                        entry(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "The Solicitor"),
                        entry(IS_JOINT, true),
                        entry(IS_DIVORCE, true),
                        entry(APPLICANT_2_SOLICITOR_ADDRESS, ADDRESS),
                        entry(IS_APP1_REPRESENTED, true),
                        entry(ADDRESS_BASED_OVERSEAS, false));
    }
}
