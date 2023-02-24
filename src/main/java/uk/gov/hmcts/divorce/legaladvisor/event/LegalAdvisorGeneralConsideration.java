package uk.gov.hmcts.divorce.legaladvisor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class LegalAdvisorGeneralConsideration implements CCDConfig<CaseData, State, UserRole> {

    public static final String LEGAL_ADVISOR_GENERAL_CONSIDERATION = "legal-advisor-general-consideration";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_GENERAL_CONSIDERATION)
            .forStateTransition(AwaitingGeneralConsideration, GeneralConsiderationComplete)
            .name("General Consideration")
            .description("General Consideration")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER))
            .page("generalConsiderationResponse")
            .pageLabel("General consideration response")
            .complex(CaseData::getGeneralReferral)
                .mandatory(GeneralReferral::getGeneralReferralDecision)
                .mandatory(GeneralReferral::getGeneralReferralDecisionReason)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Legal advisor general consideration about to submit callback invoked. CaseID: {}", details.getId());

        final CaseData caseData = details.getData();
        final GeneralReferral copyOfGeneralReferral = caseData.getGeneralReferral().toBuilder().build();

        copyOfGeneralReferral.setGeneralReferralDecisionDate(LocalDate.now(clock));

        final ListValue<GeneralReferral> generalReferralListValue = ListValue.<GeneralReferral>builder()
            .id(UUID.randomUUID().toString())
            .value(copyOfGeneralReferral)
            .build();

        if (isNull(caseData.getGeneralReferrals())) {
            caseData.setGeneralReferrals(singletonList(generalReferralListValue));
        } else {
            caseData.getGeneralReferrals().add(0, generalReferralListValue);
        }

        // Reset all fields apart from urgent case flag as it is still required by agents to filter cases.
        caseData.setGeneralReferral(
            GeneralReferral
                .builder()
                .generalReferralUrgentCase(caseData.getGeneralReferral().getGeneralReferralUrgentCase())
                .build()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
