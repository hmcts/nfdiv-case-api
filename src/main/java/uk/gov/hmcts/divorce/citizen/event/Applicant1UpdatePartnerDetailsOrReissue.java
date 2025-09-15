package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.caseworker.service.task.SetServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Optional;

import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.NoResponsePartnerNewEmailOrAddress.CONTACT_DETAILS_UPDATED;
import static uk.gov.hmcts.divorce.divorcecase.model.NoResponseSendPapersAgainOrTrySomethingElse.PAPERS_SENT;
import static uk.gov.hmcts.divorce.divorcecase.model.NoResponseSendPapersAgainOrTrySomethingElse.SEND_PAPERS_AGAIN;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@RequiredArgsConstructor
@Slf4j
public class Applicant1UpdatePartnerDetailsOrReissue implements CCDConfig<CaseData, State, UserRole> {
    public static final String UPDATE_PARTNER_DETAILS_OR_REISSUE = "update-partner-details-or-reissue";

    private final IdamService idamService;
    private final SetServiceType setServiceType;
    private final SetPostIssueState setPostIssueState;

    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(UPDATE_PARTNER_DETAILS_OR_REISSUE)
            .forStates(AwaitingAos, AosOverdue, AwaitingDocuments, AwaitingService)
            .showCondition(NEVER_SHOW)
            .name("Update details or reissue")
            .description("Update details or reissue")
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE, LEGAL_ADVISOR)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        var noResponseJourney = Optional.of(caseData.getApplicant1())
                .map(Applicant::getInterimApplicationOptions)
                .map(InterimApplicationOptions::getNoResponseJourneyOptions)
                .orElseGet(() -> NoResponseJourneyOptions.builder().build());

        var newAddress = noResponseJourney.getNoResponsePartnerAddress();
        var newEmail = noResponseJourney.getNoResponsePartnerEmailAddress();
        var applicant2 = caseData.getApplicant2();
        var updateNewEmailOrAddress = noResponseJourney.getNoResponsePartnerNewEmailOrAddress();

        if (SEND_PAPERS_AGAIN.equals(noResponseJourney.getNoResponseSendPapersAgainOrTrySomethingElse())) {
            caseData.getApplicant1().getInterimApplicationOptions().setNoResponseJourneyOptions(
                NoResponseJourneyOptions.builder().noResponseSendPapersAgainOrTrySomethingElse(PAPERS_SENT).build());
        } else if (updateNewEmailOrAddress != null) {
            switch (updateNewEmailOrAddress) {
                case ADDRESS -> updateAddress(details, newAddress, noResponseJourney);

                case EMAIL -> applicant2.setEmail(newEmail);

                case EMAIL_AND_ADDRESS -> {
                    applicant2.setEmail(newEmail);
                    updateAddress(details, newAddress, noResponseJourney);
                }

                default -> log.info("Contact details updated");
            }
            noResponseJourney.setNoResponsePartnerNewEmailOrAddress(CONTACT_DETAILS_UPDATED);
        }

        caseData.getApplication().setReissueOption(ReissueOption.REISSUE_CASE);

        caseTasks(setPostIssueState).run(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} submitted callback invoked for case id: {}", UPDATE_PARTNER_DETAILS_OR_REISSUE, details.getId());

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        ccdUpdateService
            .submitEvent(details.getId(), CASEWORKER_REISSUE_APPLICATION, user, serviceAuth);

        return SubmittedCallbackResponse.builder().build();
    }

    private void updateAddress(CaseDetails<CaseData, State> caseDetails,
                               AddressGlobalUK newAddress, NoResponseJourneyOptions noResponseJourney) {
        Applicant applicant2 = caseDetails.getData().getApplicant2();
        applicant2.setAddress(newAddress);
        applicant2.setAddressOverseas(noResponseJourney.getNoResponsePartnerAddressOverseas());
        boolean partnerAddressOverseas = YesOrNo.YES.equals(noResponseJourney.getNoResponsePartnerAddressOverseas());
        boolean partnerAddressOutsideEnglandOrWales = YesOrNo.NO.equals(noResponseJourney.getNoResponseRespondentAddressInEnglandWales());
        if (partnerAddressOverseas || partnerAddressOutsideEnglandOrWales) {
            caseDetails.getData().getApplication().setServiceMethod(ServiceMethod.PERSONAL_SERVICE);
        }

        caseTasks(setServiceType).run(caseDetails);
    }
}
