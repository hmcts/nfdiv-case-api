package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralApplication.CASEWORKER_REJECT_GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralApplication.CASE_ALREADY_ISSUED_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralApplication.CASE_MUST_BE_ISSUED_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralApplication.INVALID_STATE_ERROR;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.formatter;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithMarriageDate;

@ExtendWith(MockitoExtension.class)
class CaseworkerRejectGeneralApplicationTest {
    @InjectMocks
    private CaseworkerRejectGeneralApplication caseworkerRejectGeneralApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRejectGeneralApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REJECT_GENERAL_APPLICATION);
    }

    @Test
    void shouldPopulateSelectedGeneralApplicationInAboutToStart() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        caseData.setGeneralApplications(generalApplications);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralApplication.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
        assertThat(response.getData().getGeneralApplications()).isNotNull();
        assertThat(response.getData().getGeneralApplications()).hasSize(2);
    }

    @Test
    void shouldReturnValidationErrorWhenPreSubmissionStateSelectedByCaseworker() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(AwaitingApplicant1Response)
                .build()
            ).build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralApplication.midEvent(details, null);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().getFirst()).isEqualTo(INVALID_STATE_ERROR);
    }

    @Test
    void shouldReturnValidationErrorWhenMovingToPostIssuedStateWhenNoIssueDate() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(Holding)
                .build()
            ).build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralApplication.midEvent(details, null);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().getFirst()).isEqualTo(CASE_MUST_BE_ISSUED_ERROR);
    }

    @Test
    void shouldReturnValidationErrorWhenMovingToPreIssuedStateWhenCaseAlreadyIssued() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(Submitted)
                .issueDate(LocalDate.now())
                .build()
            ).build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralApplication.midEvent(details, null);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().getFirst()).isEqualTo(CASE_ALREADY_ISSUED_ERROR);
    }

    @Test
    void shouldRejectGeneralApplicationAboutToSubmit() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        var generalApplicationList = new ArrayList<>(generalApplications);

        caseData.setGeneralApplications(generalApplicationList);

        caseData.getGeneralReferral().setSelectedGeneralApplication(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(generalApplications.getLast().getValue().getLabel(1, formatter)).build()
            ).build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejectGeneralApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getGeneralApplications().size()).isEqualTo(1);
    }

    private List<ListValue<GeneralApplication>> buildListOfGeneralApplications() {
        return List.of(
            ListValue.<GeneralApplication>builder().value(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
                    .generalApplicationSubmittedOnline(YesOrNo.YES)
                    .generalApplicationFee(
                        FeeDetails.builder()
                            .serviceRequestReference(TEST_SERVICE_REFERENCE)
                            .paymentReference(TEST_SERVICE_REFERENCE)
                            .build()
                    )
                    .build()
            ).build(),
            ListValue.<GeneralApplication>builder().value(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.DISCLOSURE_VIA_DWP)
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
