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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralApplicationPayment.CASEWORKER_GENERAL_APPLICATION_PAYMENT;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.formatter;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithMarriageDate;

@ExtendWith(MockitoExtension.class)
class CaseworkerGeneralApplicationPaymentTest {

    @InjectMocks
    private CaseworkerGeneralApplicationPayment generalApplicationPayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalApplicationPayment.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_GENERAL_APPLICATION_PAYMENT);
    }

    @Test
    void shouldPopulateSelectedGeneralApplicationInAboutToStart() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        caseData.setGeneralApplications(generalApplications);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AosOverdue);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalApplicationPayment.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
        assertThat(response.getData().getGeneralApplications()).isNotNull();
        assertThat(response.getData().getGeneralApplications()).hasSize(2);
        assertThat(response.getData().getGeneralReferral().getSelectedGeneralApplication().getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldSetServicePaymentMethodForSelectedGeneralApplication() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        var generalApplicationList = new ArrayList<>(generalApplications);

        caseData.setGeneralApplications(generalApplicationList);

        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);

        caseData.getGeneralReferral().setSelectedGeneralApplication(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(generalApplications.getFirst().getValue().getLabel(0, formatter)).build()
            ).build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalApplicationPayment.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getGeneralApplications().get(0).getValue().getGeneralApplicationFee().getPaymentMethod())
            .isEqualTo(ServicePaymentMethod.FEE_PAY_BY_PHONE);
        assertThat(response.getData().getGeneralApplications().get(0).getValue().getGeneralApplicationFee().getHasCompletedOnlinePayment())
            .isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldResetApplicantGenAppServiceRequestForSelectedGeneralApplication() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        var generalApplicationList = new ArrayList<>(generalApplications);

        caseData.setGeneralApplications(generalApplicationList);

        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);

        caseData.getGeneralReferral().setSelectedGeneralApplication(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(generalApplications.getFirst().getValue().getLabel(0, formatter)).build()
            ).build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalApplicationPayment.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant1().getGeneralAppServiceRequest()).isNull();
    }

    @Test
    void shouldSetStateToGeneralApplicationReceivedIfNonWelshApplication() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        var generalApplicationList = new ArrayList<>(generalApplications);

        caseData.setGeneralApplications(generalApplicationList);

        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);

        caseData.getGeneralReferral().setSelectedGeneralApplication(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(generalApplications.getFirst().getValue().getLabel(0, formatter)).build()
            ).build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalApplicationPayment.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(GeneralApplicationReceived);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfWelshApplication() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        var generalApplicationList = new ArrayList<>(generalApplications);

        caseData.setGeneralApplications(generalApplicationList);

        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        caseData.getGeneralReferral().setSelectedGeneralApplication(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(generalApplications.getFirst().getValue().getLabel(0, formatter)).build()
            ).build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalApplicationPayment.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
    }

    private List<ListValue<GeneralApplication>> buildListOfGeneralApplications() {
        return List.of(
            ListValue.<GeneralApplication>builder().value(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
                    .generalApplicationSubmittedOnline(YesOrNo.YES)
                    .generalApplicationParty(GeneralParties.APPLICANT)
                    .generalApplicationFee(
                        FeeDetails.builder()
                            .serviceRequestReference(TEST_SERVICE_REFERENCE)
                            .paymentReference(TEST_SERVICE_REFERENCE)
                            .hasCompletedOnlinePayment(YesOrNo.NO)
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
                            .hasCompletedOnlinePayment(YesOrNo.YES)
                            .build()
                    )
                    .generalApplicationReceivedDate(LocalDateTime.of(2022, 1, 1, 1, 1, 1))
                    .build()
            ).build()
        );
    }
}
