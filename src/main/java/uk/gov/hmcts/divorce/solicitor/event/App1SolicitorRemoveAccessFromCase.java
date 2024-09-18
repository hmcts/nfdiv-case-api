package uk.gov.hmcts.divorce.solicitor.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class App1SolicitorRemoveAccessFromCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_REMOVE_ACCESS = "solicitor-remove-access";
    public static final String REPRESENTATIVE_REMOVED_LABEL = "# Representative removed";

    public static final String REPRESENTATIVE_REMOVED_STATUS_LABEL = """
        ### What happens next

        The court will consider your withdrawal request.""";
    public static final String IS_NO_LONGER_REPRESENTING = " is no longer representing ";
    public static final String IN_THIS_CASE = " in this case.";
    public static final String ALL_OTHER_PARTIES_HAVE_BEEN_NOTIFIED_ABOUT_THIS_CHANGE =
        " All other parties have been notified about this change\n\n";
    private final ChangeOfRepresentativeService changeOfRepresentativeService;
    private final NoticeOfChangeService noticeOfChangeService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SOLICITOR_REMOVE_ACCESS)
            .forStates(POST_SUBMISSION_STATES)
            .name("Solicitor remove access")
            .description(SOLICITOR_REMOVE_ACCESS)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("SolicitorRemoveAccessFromCase about to submit callback invoked for Case Id: {}", details.getId());

        Solicitor defaultSolicitor = changeOfRepresentativeService.solicitorWithDefaultOrganisationPolicy(
            new Solicitor(), APPLICANT_1_SOLICITOR);
        Applicant applicant = details.getData().getApplicant1();
        applicant.setSolicitor(defaultSolicitor);
        applicant.setSolicitorRepresented(NO);


        noticeOfChangeService.revokeCaseAccess(
            details.getId(), beforeDetails.getData().getApplicant1(), List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole()));

        //TODO
        //we should probably invite app1 back to the case if we have an email
        //we should send notifications
        //waiting on clarification for how this should function
       /* String sol1Email = Optional.ofNullable(beforeDetails.getData().getApplicant1())
            .map(applicant -> applicant.getSolicitor())
            .map(solicitor -> solicitor.getEmail())
            .orElse(null);
        */
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("SolicitorRemoveAccessFromCase invoked for Case Id: {}", details.getId());

        StringBuilder legalRepAndLipNames = new StringBuilder();
        Map<String, List<String>> legalRepAndLipNameMapping = new HashMap<>();

        legalRepAndLipNames.append("\n");
        legalRepAndLipNameMapping.forEach((key, value) -> legalRepAndLipNames.append(key)
            .append(IS_NO_LONGER_REPRESENTING)
            .append(String.join(", ", value))
            .append(IN_THIS_CASE)
        );

        String representativeRemovedBodyPrefix = legalRepAndLipNames.append(
                ALL_OTHER_PARTIES_HAVE_BEEN_NOTIFIED_ABOUT_THIS_CHANGE)
            .append(REPRESENTATIVE_REMOVED_STATUS_LABEL).toString();
        return SubmittedCallbackResponse.builder().confirmationHeader(
            REPRESENTATIVE_REMOVED_LABEL).confirmationBody(
            representativeRemovedBodyPrefix
        ).build();
    }
}
