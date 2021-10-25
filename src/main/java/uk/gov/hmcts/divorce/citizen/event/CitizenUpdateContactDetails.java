package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.idam.client.models.User;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class CitizenUpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_UPDATE_CONTACT_DETAILS = "citizen-update-contact-details";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_UPDATE_CONTACT_DETAILS)
            .forAllStates()
            .name("Patch a case contact details")
            .description("Patch a case contact details for correct applicant")
            .retries(120, 120)
            .grant(READ_UPDATE, CITIZEN)
            .grant(READ_UPDATE, APPLICANT_2)
            .grant(READ, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen update contact details about to submit callback invoked");

        User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));

        CaseData updatedData = details.getData();
        CaseData data = beforeDetails.getData();

        if (user.getUserDetails().getRoles().contains(APPLICANT_2)) {
            data.getApplicant2().setHomeAddress(updatedData.getApplicant2().getHomeAddress());
            data.getApplicant2().setPhoneNumber(updatedData.getApplicant2().getPhoneNumber());
        } else {
            data.getApplicant1().setHomeAddress(updatedData.getApplicant1().getHomeAddress());
            data.getApplicant1().setPhoneNumber(updatedData.getApplicant1().getPhoneNumber());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
