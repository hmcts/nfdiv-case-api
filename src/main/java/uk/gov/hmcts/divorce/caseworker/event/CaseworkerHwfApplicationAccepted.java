package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerHwfApplicationAccepted implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_HWF_APPLICATION_ACCEPTED = "caseworker-hwf-application-accepted";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_HWF_APPLICATION_ACCEPTED)
            .forStates(AwaitingHWFDecision, AwaitingPayment, AwaitingDocuments)
            .name("HWF application accepted")
            .description("HWF application accepted")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        if (caseData.getApplicationType().isSole()
            && NO.equals(caseData.getApplication().getApplicant1KnowsApplicant2Address())
            && YES.equals(caseData.getApplication().getApplicant1WantsToHavePapersServedAnotherWay())) {
            details.setState(AwaitingDocuments);
        } else {
            details.setState(Submitted);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }
}