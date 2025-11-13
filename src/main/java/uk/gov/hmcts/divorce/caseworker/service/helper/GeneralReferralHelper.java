package uk.gov.hmcts.divorce.caseworker.service.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class GeneralReferralHelper {

    private final Clock clock;

    public GeneralReferral buildGeneralReferral(GeneralApplication generalApplication) {
        return GeneralReferral.builder()
            .generalReferralReason(GeneralReferralReason.GENERAL_APPLICATION_REFERRAL)
            .generalReferralFraudCase(YesOrNo.NO)
            .generalReferralUrgentCase(YesOrNo.NO)
            .generalApplicationFrom(generalApplication.getGeneralApplicationParty())
            .generalApplicationReferralDate(LocalDate.now(clock))
            .generalApplicationAddedDate(generalApplication.getGeneralApplicationReceivedDate().toLocalDate())
            .generalReferralType(GeneralReferralType.DISCLOSURE_VIA_DWP)
            .generalReferralFee(generalApplication.getGeneralApplicationFee())
            .generalReferralJudgeOrLegalAdvisorDetails(
                "Please refer to the Search Government Records application in the general applications tab"
            )
            .build();
    }
}
