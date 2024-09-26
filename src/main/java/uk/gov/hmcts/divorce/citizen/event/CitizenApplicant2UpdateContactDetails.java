package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Slf4j
@Component
public class CitizenApplicant2UpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS = "citizen-applicant2-update-contact-details";

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private DivorceApplicationRemover divorceApplicationRemover;

    @Autowired
    private GenerateApplication generateApplication;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS)
            .forAllStates()
            .showCondition(NEVER_SHOW)
            .name("Update respondent contact info")
            .description("Contact details changed by respondent")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN, APPLICANT_2)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen applicant 2 update contact details about to submit callback invoked");
        CaseData updatedData = details.getData();
        CaseData data = beforeDetails.getData();

        if (!ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), details.getId())) {

            data.getApplicant2().setPhoneNumber(updatedData.getApplicant2().getPhoneNumber());

            boolean contactPrivacyChanged = updatedData.getApplicant2().isConfidentialContactDetails()
                != data.getApplicant2().isConfidentialContactDetails();
            data.getApplicant2().setContactDetailsType(updatedData.getApplicant2().getContactDetailsType());

            boolean addressChanged = isAddressChanged(data, updatedData);
            data.getApplicant2().setAddress(updatedData.getApplicant2().getAddress());

            if ((addressChanged || contactPrivacyChanged) && isValidState(details.getState())) {
                log.info("Regenerating divorce application");
                caseTasks(
                    divorceApplicationRemover,
                        generateApplication)
                    .run(details);

                data = details.getData();
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private boolean isAddressChanged(CaseData data, CaseData updatedData) {

        String oldAddress = AddressUtil.getPostalAddress(data.getApplicant2().getAddress());
        String newAddress = AddressUtil.getPostalAddress(updatedData.getApplicant2().getAddress());

        if (oldAddress == null && newAddress != null) {
            return true;
        }

        return oldAddress != null && !oldAddress.equalsIgnoreCase(newAddress);
    }

    private boolean isValidState(State state) {
        return AwaitingAos.equals(state) || AosOverdue.equals(state) || AosDrafted.equals(state);
    }
}
