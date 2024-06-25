package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenUpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_UPDATE_CONTACT_DETAILS = "citizen-update-contact-details";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_UPDATE_CONTACT_DETAILS)
            .forAllStates()
            .showCondition(NEVER_SHOW)
            .name("Patch a case contact details")
            .description("Patch a case contact details for correct applicant")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN, APPLICANT_2)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen update contact details about to submit callback invoked for Case Id: {}", details.getId());
        CaseData updatedData = details.getData();
        CaseData data = beforeDetails.getData();

        if (ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), details.getId())) {
            data.getApplicant1().setAddress(updatedData.getApplicant1().getAddress());
            data.getApplicant1().setPhoneNumber(updatedData.getApplicant1().getPhoneNumber());
            data.getApplicant1().setContactDetailsType(updatedData.getApplicant1().getContactDetailsType());
        } else {
            data.getApplicant2().setAddress(updatedData.getApplicant2().getAddress());
            data.getApplicant2().setPhoneNumber(updatedData.getApplicant2().getPhoneNumber());
            data.getApplicant2().setContactDetailsType(updatedData.getApplicant2().getContactDetailsType());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
