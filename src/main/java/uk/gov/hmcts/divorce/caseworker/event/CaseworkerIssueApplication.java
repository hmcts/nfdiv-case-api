package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.IssueApplicationService;
import uk.gov.hmcts.divorce.caseworker.service.task.SetServiceType;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.exception.InvalidDataException;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDwpResponse;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueSolicitorServicePack.SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerIssueApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ISSUE_APPLICATION = "caseworker-issue-application";

    private static final String ALWAYS_HIDE = "marriageApplicant1Name=\"ALWAYS_HIDE\"";

    private static final String WARNING_LABEL = "### There is no address for the Respondent, "
        + "you need to provide a reason for issuing the application without the address for the respondent";

    private final IssueApplicationService issueApplicationService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final SetServiceType setServiceType;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_ISSUE_APPLICATION)
            .forStates(Submitted, AwaitingDocuments, InformationRequested, RequestedInformationSubmitted, AwaitingDwpResponse)
            .name("Application issue")
            .description("Application issued")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("issueApplication")
            .pageLabel("Issue Divorce Application")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnership, ALWAYS_HIDE)
            .done()
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .complex(CaseData::getApplication)
                .readonlyNoSummary(Application::getBeingIssuedWithoutAddress, ALWAYS_HIDE)
            .done()
            .label("warningIssueWithoutAddress", WARNING_LABEL, "beingIssuedWithoutAddress=\"Yes\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getReasonIssuedWithoutAddress, "beingIssuedWithoutAddress=\"Yes\"")
                .complex(Application::getMarriageDetails)
                    .optional(MarriageDetails::getDate)
                    .optional(MarriageDetails::getApplicant1Name)
                    .optional(MarriageDetails::getApplicant2Name)
                    .mandatory(MarriageDetails:: getCountryOfMarriage)
                    .mandatory(MarriageDetails::getPlaceOfMarriage)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();

        String app2Address = AddressUtil.getPostalAddress(caseData.getApplicant2().getAddress());

        boolean soleApplicationBeingIssuedWithoutApp2Address =
            caseData.getApplicationType() == ApplicationType.SOLE_APPLICATION
            && StringUtils.isEmpty(app2Address);

        if (soleApplicationBeingIssuedWithoutApp2Address) {
            caseData.getApplication().setBeingIssuedWithoutAddress(YesOrNo.YES);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker issue application about to submit callback invoked for case id: {}", details.getId());

        CaseData caseData = details.getData();
        caseData.getApplication().setBeingIssuedWithoutAddress(null);

        try {
            final CaseDetails<CaseData, State> result = issueApplicationService.issueApplication(details);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(result.getData())
                .state(result.getState())
                .build();
        } catch (InvalidDataException exception) {
            log.info("Data not valid for application issue, case id: {}", details.getId(), exception);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(exception.getErrors())
                .build();
        }
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker issue application submitted callback invoked for case id: {}", details.getId());

        if (details.getData().getApplication().isSolicitorServiceMethod()) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuthorization = authTokenGenerator.generate();

            log.info("Submitting system-issue-solicitor-service-pack event for case id: {}", details.getId());
            ccdUpdateService.submitEvent(details.getId(), SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK, user, serviceAuthorization);
        }

        issueApplicationService.sendNotifications(details);

        return SubmittedCallbackResponse.builder().build();
    }
}
