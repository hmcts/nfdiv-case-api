package uk.gov.hmcts.divorce.caseworker.service.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType.DISCLOSURE_VIA_DWP;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason.GENERAL_APPLICATION_REFERRAL;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;


@ExtendWith(MockitoExtension.class)
class GeneralReferralHelperTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private GeneralReferralHelper generalReferralHelper;

    @Test
    void shouldBuildGeneralReferral() {
        setMockClock(clock);

        final CaseData caseData = TestDataHelper.caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationType(DISCLOSURE_VIA_DWP)
            .generalApplicationParty(GeneralParties.APPLICANT)
            .generalApplicationTypeOtherComments("some comments")
            .generalApplicationReceivedDate(LocalDateTime.now())
            .build()
        );

        GeneralReferral referral = generalReferralHelper.buildGeneralReferral(caseData.getGeneralApplication());

        assertThat(referral.getGeneralReferralReason()).isEqualTo(GENERAL_APPLICATION_REFERRAL);
        assertThat(referral.getGeneralReferralUrgentCase()).isEqualTo(NO);
        assertThat(referral.getGeneralReferralUrgentCase()).isEqualTo(NO);
        assertThat(referral.getGeneralApplicationFrom()).isEqualTo(GeneralParties.APPLICANT);
        assertThat(referral.getGeneralReferralType()).isEqualTo(GeneralReferralType.DISCLOSURE_VIA_DWP);
        assertThat(referral.getGeneralApplicationReferralDate()).isEqualTo(LocalDate.of(2025, 11, 13));
        assertThat(referral.getGeneralReferralJudgeOrLegalAdvisorDetails())
            .isEqualTo("Please refer to the Search Government Records application in the general applications tab");
    }
}
