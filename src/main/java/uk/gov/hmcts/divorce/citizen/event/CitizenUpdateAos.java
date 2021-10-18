package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class CitizenUpdateAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_UPDATE_AOS = "citizen-update-aos";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_UPDATE_AOS)
            .forStates(AosDrafted, AosOverdue)
            .name("Patch a case in aos")
            .description("Patch a case in aos as applicant 2")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, APPLICANT_2)
            .grant(READ, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen update Aos about to submit callback invoked");

        CaseData data = details.getData();

        if (data.getAcknowledgementOfService().getConfirmDisputeApplication() == YesOrNo.NO
            && data.getAcknowledgementOfService().getDisputeApplication() == YesOrNo.YES) {

            data.getAcknowledgementOfService().setDisputeApplication(null);
            data.getAcknowledgementOfService().setConfirmDisputeApplication(null);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
