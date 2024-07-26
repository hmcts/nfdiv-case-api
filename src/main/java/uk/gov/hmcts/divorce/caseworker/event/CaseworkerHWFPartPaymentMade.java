package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerHWFPartPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_HWF_PART_PAYMENT_MADE = "caseworker-hwf-part-payment-made";
    public static final String EVENT_NAME_AND_DESCRIPTION = "HWF part payment made";

    private final CaseworkerHwfApplicationAndPaymentHelper caseworkerHwfApplicationAndPaymentHelper;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_HWF_PART_PAYMENT_MADE)
            .forStateTransition(EnumSet.of(AwaitingDocuments, AwaitingHWFPartPayment, AwaitingHWFEvidence), Submitted)
            .name(EVENT_NAME_AND_DESCRIPTION)
            .description(EVENT_NAME_AND_DESCRIPTION)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(SOLICITOR, LEGAL_ADVISOR, CITIZEN));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_HWF_PART_PAYMENT_MADE, details.getId());

        caseworkerHwfApplicationAndPaymentHelper.setRequiredCaseFieldsForPostSubmissionCase(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }
}
