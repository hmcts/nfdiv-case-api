package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.Applicant2HowToRespondToApplication;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolAosAskCourtToDelay;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolAosJurisdiction;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolAosOtherProceedings;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolConfirmContactDetails;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolReviewApplicant1Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Slf4j
@Component
public class DraftAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String DRAFT_AOS = "draft-aos";
    public static final String DRAFT_AOS_ALREADY_SUBMITTED_ERROR
        = "The Acknowledgement Of Service has already been submitted.";
    protected static final List<CcdPageConfiguration> pages = asList(
        new Applicant2SolConfirmContactDetails(),
        new Applicant2SolReviewApplicant1Application(),
        new Applicant2HowToRespondToApplication(),
        new Applicant2SolAosJurisdiction(),
        new Applicant2SolAosAskCourtToDelay(),
        new Applicant2SolAosOtherProceedings()
    );
    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(DRAFT_AOS)
            .forStates(ArrayUtils.addAll(AOS_STATES, AwaitingAos, AosOverdue, OfflineDocumentReceived, AwaitingService))
            .name("Draft AoS")
            .description("Draft Acknowledgement of Service")
            .showCondition("applicationType=\"soleApplication\" AND aosIsDrafted!=\"Yes\"")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .endButtonLabel("Save AoS Response")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE,
                SYSTEMUPDATE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Draft AoS about to start callback invoked for Case Id: {}", details.getId());

        final var caseData = details.getData();

        final List<String> errors = validateDraftAos(caseData);
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(errors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseTasks(addMiniApplicationLink)
                .run(details)
                .getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> before) {

        log.info("Draft AoS about to submit callback invoked for Case Id: {}", details.getId());

        var state = details.getState() == AwaitingAos || details.getState() == AosOverdue ? AosDrafted : details.getState();

        details.getData().getAcknowledgementOfService().setAosIsDrafted(YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }

    private List<String> validateDraftAos(final CaseData caseData) {
        final var acknowledgementOfService = caseData.getAcknowledgementOfService();
        final List<String> errors = new ArrayList<>();

        if (null != acknowledgementOfService && null != acknowledgementOfService.getDateAosSubmitted()) {
            errors.add(DRAFT_AOS_ALREADY_SUBMITTED_ERROR);
        }

        if (!isNull(acknowledgementOfService) && YES.equals(acknowledgementOfService.getConfirmReadPetition())) {
            errors.add("The Acknowledgement Of Service has already been drafted.");
        }

        if (isNull(caseData.getApplication().getIssueDate())) {
            errors.add("You cannot draft the AoS until the case has been issued. Please wait for the case to be issued.");
        }

        return errors;
    }
}
