package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGenAppHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralApplicationPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingDate;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingOutcome;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerGeneralApplicationHwfEvidenceRequired implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_GENERAL_APPLICATION_HWF_EVIDENCE_REQUIRED = "caseworker-gen-app-hwf-evidence-required";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_GENERAL_APPLICATION_HWF_EVIDENCE_REQUIRED)
            .forStateTransition(EnumSet.of(RespondentFinalOrderRequested, PendingHearingDate, PendingHearingOutcome, AwaitingServicePayment,
                AwaitingGeneralApplicationPayment, AwaitingGeneralReferralPayment), AwaitingGenAppHWFEvidence)
            .name("GenAppHWF evidence required")
            .description("General application HWF evidence required")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
