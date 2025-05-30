package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
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
@RequiredArgsConstructor
public class CitizenApplicant2UpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS = "citizen-applicant2-update-contact-details";

    private final HttpServletRequest request;

    private final CcdAccessService ccdAccessService;

    private final DivorceApplicationRemover divorceApplicationRemover;

    private final GenerateApplication generateApplication;

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
            boolean hasChanged = isAddressChanged(data, updatedData)
                || updatedData.getApplicant2().isConfidentialContactDetails() != data.getApplicant2().isConfidentialContactDetails();
            updateApplicant2(data.getApplicant2(),updatedData.getApplicant2());
            if (hasChanged && isValidState(details.getState())) {
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

    private void updateApplicant2(Applicant dataApplicant2, Applicant updatedApplicant2) {
        dataApplicant2.setPhoneNumber(updatedApplicant2.getPhoneNumber());
        dataApplicant2.setContactDetailsType(updatedApplicant2.getContactDetailsType());
        dataApplicant2.setAddress(updatedApplicant2.getAddress());
        dataApplicant2.setInRefuge(updatedApplicant2.isConfidentialContactDetails()
            ? updatedApplicant2.getInRefuge() : YesOrNo.NO);
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
