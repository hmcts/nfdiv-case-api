package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.SoleAosSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class CitizenSubmitAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SUBMIT_AOS = "citizen-submit-aos";

    @Autowired
    private SoleAosSubmittedNotification soleAosSubmittedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_SUBMIT_AOS)
            .forState(AosDrafted)
            .name("Citizen draft AoS")
            .description("Citizen submit Acknowledgement of Service")
            .grant(CREATE_READ_UPDATE, APPLICANT_2)
            .grant(READ, SUPER_USER);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen respondent submit Aos about to submit callback invoked");
        CaseData data = details.getData();

        State state;


        if (data.getAcknowledgementOfService().getDisputeApplication() == YesOrNo.YES) {
//            soleAosSubmittedNotification.sendApplicationDisputedToApplicant(data, details.getId());
//            soleAosSubmittedNotification.sendApplicationDisputedToRespondent(data, details.getId());
            state = State.PendingDispute;
        } else {
            soleAosSubmittedNotification.sendApplicationNotDisputedToApplicant(data, details.getId());
            soleAosSubmittedNotification.sendApplicationNotDisputedToRespondent(data, details.getId());
            state = State.Holding;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }
}
