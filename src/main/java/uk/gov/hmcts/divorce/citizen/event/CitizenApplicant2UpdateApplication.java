package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class CitizenApplicant2UpdateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_APPLICANT2_UPDATE = "citizen-applicant2-update-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_APPLICANT2_UPDATE)
            .forStates(AwaitingApplicant2Response, AosDrafted, ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Patch a joint case")
            .description("Patch a joint divorce or dissolution as applicant 2")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, APPLICANT_2)
            .grant(READ, SUPER_USER);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        if (details.getState() == AosDrafted
            && data.getAcknowledgementOfService().getConfirmDisputeApplication() == YesOrNo.NO
            && data.getAcknowledgementOfService().isDisputed()) {

            data.getAcknowledgementOfService().setHowToRespondApplication(null);
            data.getAcknowledgementOfService().setConfirmDisputeApplication(null);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
