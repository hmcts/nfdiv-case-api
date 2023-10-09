package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2ApprovedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DraftApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.Applicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_JOINT_APPLICANT_2_ANSWERS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JOINT_DIVORCE_APPLICANT_2_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class Applicant2ApproveTest {

    @Mock
    private Applicant2ApprovedNotification applicant2ApprovedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DraftApplicationTemplateContent draftApplicationTemplateContent;

    @InjectMocks
    private Applicant2Approve applicant2Approve;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2Approve.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_APPROVE);
    }

    @Test
    void givenEventStartedWithEmptyCaseThenGiveValidationErrors() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(4);
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant2FirstName cannot be empty or null");
    }

    @Test
    void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2Approve.aboutToSubmit(details, details);

        assertThat(response.getErrors().size()).isEqualTo(4);
        assertThat(response.getErrors()).containsExactlyInAnyOrder(
            "Applicant2StatementOfTruth cannot be empty or null",
            "MarriageApplicant2Name cannot be empty or null",
            "Applicant2FirstName cannot be empty or null",
            "Applicant2LastName cannot be empty or null"
        );
    }

    @Test
    void givenEventStartedWithValidCaseThenChangeStateApplicant2ApprovedAndSendEmailAndGenerateApplicationDocument() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .address("App1 Sol Address")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .address("App2 Sol Address")
                .build()
        );

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        caseDetails.setState(State.AwaitingApplicant2Response);

        final List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        caseData.getDocuments().setDocumentsGenerated(documentsGenerated);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant2ApprovedNotification, caseData, caseDetails.getId());
        assertThat(response.getState()).isEqualTo(State.Applicant2Approved);
    }

    @Test
    void shouldRenderDocumentIfJointApplicationAndApplicant1IsRepresented() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Map<String, Object> templateContent = new HashMap<>();

        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getApplicant1HelpWithFees().setNeedHelp(YES);
        caseData.getApplication().getApplicant2HelpWithFees().setNeedHelp(NO);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);

        final List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        caseData.getDocuments().setDocumentsGenerated(documentsGenerated);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(State.AwaitingApplicant2Response);

        when(draftApplicationTemplateContent.apply(caseData, caseId)).thenReturn(templateContent);

        applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                templateContent,
                caseId,
                DIVORCE_JOINT_APPLICANT_2_ANSWERS,
                caseData.getApplicant1().getLanguagePreference(),
                JOINT_DIVORCE_APPLICANT_2_ANSWERS_DOCUMENT_NAME + caseId
            );
    }

    @Test
    void shouldNotRenderDocumentIfNotJointApplicationAndApplicant1IsRepresented() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().getApplicant1HelpWithFees().setNeedHelp(YES);
        caseData.getApplication().getApplicant2HelpWithFees().setNeedHelp(NO);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(State.AwaitingApplicant2Response);

        applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    void shouldNotRenderDocumentIfJointApplicationAndApplicant1IsNotRepresented() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getApplicant1HelpWithFees().setNeedHelp(YES);
        caseData.getApplication().getApplicant2HelpWithFees().setNeedHelp(NO);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(YES);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(State.AwaitingApplicant2Response);

        applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(caseDataDocumentService);
    }

    private CaseData setValidCaseData(CaseData caseData) {
        caseData.setApplicant1(getApplicant());
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant1().setContactDetailsType(PUBLIC);
        caseData.getApplication().setApplicant1HelpWithFees(
            HelpWithFees.builder()
                .needHelp(NO)
                .build()
        );

        caseData.getApplication().setApplicant2HelpWithFees(
            HelpWithFees.builder()
                .needHelp(NO)
                .build()
        );

        caseData.getApplicant2().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().getMarriageDetails().setApplicant1Name("Full name");
        caseData.getApplication().getMarriageDetails().setApplicant2Name("Full name");

        caseData.getApplication().getMarriageDetails().setDate(LocalDate.now().minus(2, ChronoUnit.YEARS));
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YES);

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT));
        jurisdiction.setBothLastHabituallyResident(YES);
        caseData.getApplication().setJurisdiction(jurisdiction);
        return caseData;
    }
}
