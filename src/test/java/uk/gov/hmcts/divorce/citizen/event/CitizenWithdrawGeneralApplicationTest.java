package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.service.InterimApplicationOptionsService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.citizen.event.CitizenWithdrawGeneralApplication.CITIZEN_WITHDRAW_GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getListOfDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class CitizenWithdrawGeneralApplicationTest {

    @Mock
    CcdAccessService ccdAccessService;

    @Mock
    HttpServletRequest request;

    @Mock
    InterimApplicationOptionsService interimApplicationOptionsService;

    @Mock
    DocumentRemovalService documentRemovalService;

    @InjectMocks
    private CitizenWithdrawGeneralApplication citizenWithdrawGeneralApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenWithdrawGeneralApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_WITHDRAW_GENERAL_APPLICATION);
    }

    @Test
    void shouldCallInterimApplicationOptionsServiceWhenPaymentIsNotExpectedYet() {
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
                            .generalApplicationD11JourneyOptions(GeneralApplicationD11JourneyOptions.builder()
                                    .type(GeneralApplicationType.AMEND_APPLICATION)
                                    .build())
                            .build())
                    .build())
            .build();

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
                citizenWithdrawGeneralApplication.aboutToSubmit(details, details);

        verify(interimApplicationOptionsService).resetInterimApplicationOptions(caseData.getApplicant1());
        assertThat(response.getData().getApplicant1().getInterimApplicationOptions().getInterimApplicationType()).isNull();
    }

    @Test
    void shouldRemoveGeneralApplicationFromCollection() {
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                    .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
                    .build())
            .generalApplications(generalApplications)
            .build();

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Holding);
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
                citizenWithdrawGeneralApplication.aboutToSubmit(details, details);

        verifyNoInteractions(documentRemovalService);

        assertThat(response.getState()).isEqualTo(Holding);
        assertThat(response.getData().getGeneralApplications()).isNotNull();
        assertThat(response.getData().getGeneralApplications()).hasSize(1);
        assertThat(response.getData().getGeneralApplications().get(0).getValue().getGeneralApplicationType())
                .isEqualTo(GeneralApplicationType.DEEMED_SERVICE);
    }

    @Test
    void shouldCallDocumentRemovalServiceToRemoveGeneralApplicationAnswersDocument() {
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        Document document = Document.builder().build();
        DivorceDocument genAppDoc = DivorceDocument.builder().documentLink(document).build();
        generalApplications.get(1).getValue().setGeneralApplicationDocument(genAppDoc);

        CaseData caseData = CaseData.builder()
                .applicant1(Applicant.builder()
                        .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
                        .build())
                .generalApplications(generalApplications)
                .build();

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        citizenWithdrawGeneralApplication.aboutToSubmit(details, details);

        verify(documentRemovalService).deleteDocument(document);
    }

    @Test
    void shouldCallDocumentRemovalServiceToRemoveGeneralApplicationUploadedDocuments() {
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(2);
        generalApplications.get(1).getValue().setGeneralApplicationDocuments(docs);

        CaseData caseData = CaseData.builder()
                .applicant1(Applicant.builder()
                        .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
                        .build())
                .generalApplications(generalApplications)
                .build();

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        citizenWithdrawGeneralApplication.aboutToSubmit(details, details);

        verify(documentRemovalService).deleteDocument(docs);
    }

    @Test
    void shouldSetEndStateToAwaitingAosIfPostIssueSearchGovApplicationWithdrawn() {
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        generalApplications.getLast().getValue().setGeneralApplicationType(
            GeneralApplicationType.DISCLOSURE_VIA_DWP
        );

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
                .build())
            .generalApplications(generalApplications)
            .build();
        caseData.getApplication().setIssueDate(LocalDate.now());

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenWithdrawGeneralApplication.aboutToSubmit(details, details);

        verifyNoInteractions(documentRemovalService);

        assertThat(response.getState()).isEqualTo(AwaitingAos);
        assertThat(response.getData().getGeneralApplications()).isNotNull();
        assertThat(response.getData().getGeneralApplications()).hasSize(1);
        assertThat(response.getData().getGeneralApplications().getFirst().getValue().getGeneralApplicationType())
            .isEqualTo(GeneralApplicationType.DEEMED_SERVICE);
    }

    @Test
    void shouldSetEndStateToAwaitingApplicantIfPreIssueSearchGovApplicationWithdrawn() {
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        generalApplications.getLast().getValue().setGeneralApplicationType(
            GeneralApplicationType.DISCLOSURE_VIA_DWP
        );

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
                .build())
            .generalApplications(generalApplications)
            .build();
        caseData.getApplication().setIssueDate(null);

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenWithdrawGeneralApplication.aboutToSubmit(details, details);

        verifyNoInteractions(documentRemovalService);

        assertThat(response.getState()).isEqualTo(AwaitingDocuments);
        assertThat(response.getData().getGeneralApplications()).isNotNull();
        assertThat(response.getData().getGeneralApplications()).hasSize(1);
        assertThat(response.getData().getGeneralApplications().getFirst().getValue().getGeneralApplicationType())
            .isEqualTo(GeneralApplicationType.DEEMED_SERVICE);
    }

    private List<ListValue<GeneralApplication>> buildListOfGeneralApplications() {
        return List.of(
                ListValue.<GeneralApplication>builder().value(
                        GeneralApplication.builder()
                                .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
                                .generalApplicationSubmittedOnline(YesOrNo.YES)
                                .build()
                ).build(),
                ListValue.<GeneralApplication>builder().value(
                        GeneralApplication.builder()
                                .generalApplicationType(GeneralApplicationType.WITHDRAW_POST_ISSUE)
                                .generalApplicationSubmittedOnline(YesOrNo.YES)
                                .generalApplicationParty(GeneralParties.APPLICANT)
                                .generalApplicationFee(
                                        FeeDetails.builder()
                                                .serviceRequestReference(TEST_SERVICE_REFERENCE)
                                                .build()
                                )
                                .generalApplicationReceivedDate(LocalDateTime.of(2022, 1, 1, 1, 1, 1))
                                .build()
                ).build()
        );
    }
}
