package uk.gov.hmcts.divorce.caseworker.event;

import com.google.common.collect.SetMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralReferral.CASEWORKER_GENERAL_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceMediumType.TEXT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason.GENERAL_APPLICATION_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.CASEWORKER_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerGeneralReferralTest {
    private final Instant instant = Instant.now();
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerGeneralReferral generalReferral;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm:ss a");

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalReferral.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_GENERAL_REFERRAL);
    }

    @Test
    void shouldAddSystemUpdateRoleToGrants() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalReferral.configure(configBuilder);

        final SetMultimap<UserRole, Permission> grants = Objects.requireNonNull(
            configBuilder.build().getEvents().get(CASEWORKER_GENERAL_REFERRAL)).getGrants();
        assertTrue(grants.asMap().containsKey(UserRole.SYSTEMUPDATE));
    }

    @Test
    void shouldSetGeneralApplicationLabelsForEventSelectInput() {
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(generalApplications);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse = generalReferral.aboutToStart(details);
        List<DynamicListElement> generalApplicationLabels = aboutToStartResponse.getData().getGeneralReferral()
            .getSelectedGeneralApplication().getListItems();

        assertThat(generalApplicationLabels).hasSize(2);
        assertThat(generalApplicationLabels.getFirst().getLabel()).isEqualTo(
            generalApplications.getFirst().getValue().getLabel(0, formatter)
        );
        assertThat(generalApplicationLabels.getLast().getLabel()).isEqualTo(
            generalApplications.getLast().getValue().getLabel(1, formatter)
        );
    }

    @Test
    void shouldHandleNullGeneralApplicationsWhenSettingLabels() {
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(null);
        details.getData().setGeneralApplications(null);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse = generalReferral.aboutToStart(details);
        DynamicList generalApplicationLabels = aboutToStartResponse.getData().getGeneralReferral().getSelectedGeneralApplication();

        assertThat(generalApplicationLabels.getListItems()).isEmpty();
    }

    @Test
    void shouldHandleEmptyGeneralApplicationsWhenSettingLabels() {
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(null);
        details.getData().setGeneralApplications(Collections.emptyList());
        details.getData().getGeneralReferral().setGeneralReferralType(GeneralReferralType.PERMISSION_ON_DA_OOT);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse = generalReferral.aboutToStart(details);

        assertThat(aboutToStartResponse.getData().getGeneralReferral().getGeneralReferralType())
            .isNull();
    }

    @Test
    void shouldResetGeneralReferralType() {
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(null);
        details.getData().setGeneralApplications(Collections.emptyList());

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse = generalReferral.aboutToStart(details);
        DynamicList generalApplicationLabels = aboutToStartResponse.getData().getGeneralReferral().getSelectedGeneralApplication();

        assertThat(generalApplicationLabels.getListItems()).isEmpty();
    }

    @Test
    void shouldProcessSelectedGeneralApplication() {
        setClock();

        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(generalApplications);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalReferral.aboutToSubmit(details, details);

        Applicant afterApplicant = response.getData().getApplicant1();
        List<ListValue<GeneralApplication>> afterGeneralApplications = response.getData().getGeneralApplications();
        GeneralApplication afterSearchGovApplication = afterGeneralApplications.get(1).getValue();

        assertThat(afterApplicant.getGeneralAppServiceRequest()).isNull();
        assertThat(afterApplicant.getGeneralAppPayments()).isEmpty();
        assertThat(afterGeneralApplications).hasSize(2);
        assertThat(afterGeneralApplications.getFirst()).isEqualTo(buildListOfGeneralApplications().getFirst());
        assertThat(afterGeneralApplications.getLast()).isNotEqualTo(buildListOfGeneralApplications().getLast());
        assertThat(afterSearchGovApplication.getGeneralApplicationFee().getServiceRequestReference())
            .isNull();
    }

    @Test
    void shouldNotProcessApplicationIfDifferentReferralReasonSelected() {
        setClock();

        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(generalApplications);

        details.getData().getGeneralReferral().setGeneralReferralReason(GeneralReferralReason.CASEWORKER_REFERRAL);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalReferral.aboutToSubmit(details, details);
        List<ListValue<GeneralApplication>> afterGeneralApplications = response.getData().getGeneralApplications();

        assertThat(afterGeneralApplications).isEqualTo(generalApplications);
        assertThat(response.getData().getApplicant1().getGeneralAppServiceRequest()).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldNotRemovePaymentDetailsFromPaidApplications() {
        setClock();

        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(generalApplications);
        details.getData().getGeneralReferral().setSelectedGeneralApplication(DynamicList
            .builder()
            .value(DynamicListElement.builder().label(
                generalApplications.getLast().getValue().getLabel(0, formatter)
                ).build()
            ).build()
        );

        AboutToStartOrSubmitResponse<CaseData, State> response = generalReferral.aboutToSubmit(details, details);

        List<ListValue<GeneralApplication>> afterGeneralApplications = response.getData().getGeneralApplications();

        assertThat(afterGeneralApplications).isEqualTo(generalApplications);
        assertThat(response.getData().getApplicant1().getGeneralAppServiceRequest()).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldUpdateStateToAwaitingGeneralConsiderationAndAddApplicationAddedDateToCaseDataWhenGeneralReferralFeeIsNotRequired() {
        setClock();

        final CaseData caseData = caseData();
        caseData.setGeneralReferral(generalReferral(NO));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = generalReferral.aboutToSubmit(details, details);

        assertThat(aboutToSubmitResponse.getState()).isEqualTo(AwaitingGeneralConsideration);

        final var expectedDate = LocalDate.ofInstant(instant, zoneId);
        assertThat(aboutToSubmitResponse.getData().getGeneralReferral().getGeneralApplicationAddedDate())
            .isEqualTo(expectedDate);
    }

    @Test
    void shouldUpdateStateToAwaitingGeneralReferralPaymentAndAddApplicationAddedDateToCaseDataWhenGeneralReferralFeeIsRequired() {
        setClock();

        final CaseData caseData = caseData();
        caseData.setGeneralReferral(generalReferral(YES));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmitResponse = generalReferral.aboutToSubmit(details, details);

        assertThat(aboutToSubmitResponse.getState()).isEqualTo(AwaitingGeneralReferralPayment);

        final var expectedDate = LocalDate.ofInstant(instant, zoneId);
        assertThat(aboutToSubmitResponse.getData().getGeneralReferral().getGeneralApplicationAddedDate())
            .isEqualTo(expectedDate);
    }

    private GeneralReferral generalReferral(YesOrNo feeRequired) {
        return GeneralReferral
            .builder()
            .generalApplicationReferralDate(LocalDate.now())
            .generalApplicationFrom(APPLICANT)
            .generalReferralFeeRequired(feeRequired)
            .generalReferralType(CASEWORKER_REFERRAL)
            .generalReferralReason(GENERAL_APPLICATION_REFERRAL)
            .alternativeServiceMedium(TEXT)
            .generalReferralJudgeOrLegalAdvisorDetails("some judge legal advisor details")
            .build();
    }

    private void setClock() {
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);
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

    private CaseDetails<CaseData, State> buildTestCaseDetails(List<ListValue<GeneralApplication>> generalApplications) {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);
        caseData.getApplicant1().setGeneralAppPayments(List.of(
            ListValue.<Payment>builder().value(
                Payment.builder().amount(10).build()
            ).build()
        ));
        caseData.setGeneralApplications(generalApplications);
        caseData.setGeneralReferral(generalReferral(NO));
        caseData.getGeneralReferral().setGeneralReferralReason(GeneralReferralReason.GENERAL_APPLICATION_REFERRAL);
        if (CollectionUtils.isNotEmpty(generalApplications)) {
            caseData.getGeneralReferral().setSelectedGeneralApplication(DynamicList
                .builder()
                .value(DynamicListElement.builder().label(generalApplications.getLast().getValue().getLabel(1, formatter)).build()
                ).build()
            );
        }
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        return details;
    }
}
