package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.ReIssueApplicationService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseSendPapersAgainOrTrySomethingElse;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class Applicant1UpdatePartnerDetailsOrReissue implements CCDConfig<CaseData, State, UserRole> {
    public static final String UPDATE_PARTNER_DETAILS_OR_REISSUE = "update-partner-details-or-reissue";

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;
    private final ReIssueApplicationService reIssueApplicationService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(UPDATE_PARTNER_DETAILS_OR_REISSUE)
            .forStates(AwaitingAos, AosOverdue, AwaitingDocuments, AwaitingService)
            .showCondition(NEVER_SHOW)
            .name("Update details and reissue")
            .description("Update details and reissue")
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

        if (!isEmpty(newAddress)) {
            applicant2.setAddress(newAddress);
        }

        if (!isEmpty(newEmail)) {
            applicant2.setEmail(newEmail);
            applicant2.setAddressOverseas(Objects.requireNonNullElse(applicant2.getAddressOverseas(), YesOrNo.NO));
        }

        try {

            boolean updateReissueOptionsForNewContactDetails = Optional.ofNullable(caseData.getApplicant1())
                    .map(Applicant::getInterimApplicationOptions)
                    .map(InterimApplicationOptions::getNoResponseJourneyOptions)
                    .map(NoResponseJourneyOptions::getNoResponsePartnerNewEmailOrPostalAddress)
                    .isPresent();

            if (updateReissueOptionsForNewContactDetails) {
                reIssueApplicationService.updateReissueOptionForNewContactDetails(details, details.getId());
            } else {
                reIssueApplicationService.process(details);
            }
        } catch (InvalidReissueOptionException ex) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .errors(List.of(String.format("Invalid update contact details option selected for CaseId: %s",
                            details.getId())))
                    .build();
        }

        if (processNoResponseJourneyOptions(getNoResponseJourneyOptions(caseData))) {
                details.setState(AwaitingAos);
        }       // Add logic for more options if required

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        NoResponseJourneyOptions noResponseJourneyOptions = getNoResponseJourneyOptions(details.getData());

        if (processNoResponseJourneyOptions(noResponseJourneyOptions)) {

            log.info("{} submitted callback invoked for case id: {}", UPDATE_PARTNER_DETAILS_OR_REISSUE, details.getId());

            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            ccdUpdateService
                .submitEvent(details.getId(), CASEWORKER_REISSUE_APPLICATION, user, serviceAuth);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private NoResponseJourneyOptions getNoResponseJourneyOptions(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicant1().getInterimApplicationOptions())
            .map(InterimApplicationOptions::getNoResponseJourneyOptions)
            .orElse(null);
    }

    private boolean processNoResponseJourneyOptions(NoResponseJourneyOptions options) {
        return options != null
            && options.getNoResponseSendPapersAgainOrTrySomethingElse() != null;
    }

}
