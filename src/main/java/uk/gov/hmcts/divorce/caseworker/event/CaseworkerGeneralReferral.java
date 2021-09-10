package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerGeneralReferral implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_GENERAL_REFERRAL = "caseworker-general-referral";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_GENERAL_REFERRAL)
            .forAllStates()
            .name("General referral")
            .description("General referral")
            .explicitGrants()
            .showSummary(false)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASEWORKER_COURTADMIN)
            .grant(READ, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR, SOLICITOR, CITIZEN))
            .page("generalReferral")
            .pageLabel("General referral")
            .complex(CaseData::getGeneralReferral)
                .mandatory(GeneralReferral::getGeneralReferralReason)
                .mandatory(GeneralReferral::getGeneralApplicationFrom, "generalReferralReason=\"generalApplicationReferral\"")
                .optional(GeneralReferral::getGeneralApplicationReferralDate)
                .mandatory(GeneralReferral::getGeneralReferralType)
                .mandatory(GeneralReferral::getAlternativeServiceMedium, "generalReferralType=\"alternativeServiceApplication\"")
                .mandatory(GeneralReferral::getGeneralReferralJudgeDetails)
                .mandatory(GeneralReferral::getGeneralReferralLegalAdvisorDetails)
                .mandatory(GeneralReferral::getGeneralReferralFeeRequired)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker general referral about to submit callback invoked");

        var caseDataCopy = details.getData().toBuilder().build();

        State endState = caseDataCopy.getGeneralReferral().getGeneralReferralFeeRequired().toBoolean()
            ? AwaitingGeneralReferralPayment
            : AwaitingGeneralConsideration;

        caseDataCopy.getGeneralReferral().setGeneralApplicationAddedDate(LocalDate.now(clock));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(endState)
            .build();
    }
}
