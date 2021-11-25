package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerGeneralReferralTest {
    private final Instant instant = Instant.now();
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerGeneralReferral generalReferral;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalReferral.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_GENERAL_REFERRAL);
    }

    @Test
    void shouldUpdateStateToAwaitingGeneralConsiderationAndAddApplicationAddedDateToCaseDataWhenGeneralReferralFeeIsNotRequired() {
        setClock();

        final CaseData caseData = caseData();
        caseData.setGeneralReferral(generalReferral(NO));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

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

}
