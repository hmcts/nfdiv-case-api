package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.notification.pin.PinGenerationService;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenInviteApplicant2 implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_INVITE_APPLICANT_2 = "citizen-invite-applicant2";

    @Autowired
    private PinGenerationService pinGenerationService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_INVITE_APPLICANT_2)
            .initialState(Draft)
            .name("Send Application to Applicant 2 for review")
            .description("Send Application to Applicant 2 for review")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .displayOrder(1)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();
        log.info("Generating pin to allow Applicant 2 to access the joint application");
        final String pin = pinGenerationService.generatePin();
        data.setInvitePin(pin);

        // TODO - send email (to be done in NFDIV-689)

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingApplicant2Response)
            .build();
    }
}
