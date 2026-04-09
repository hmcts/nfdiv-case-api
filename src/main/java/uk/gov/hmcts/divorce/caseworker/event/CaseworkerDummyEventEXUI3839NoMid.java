package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerDummyEventEXUI4347.EXUI_ISSUE_ID;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerDummyEventEXUI3839NoMid implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_DUMMY_EVENT_EXUI_3839_NO_MID_EVENT = "caseworker-dummy-event-exui3839-no-mid-event";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final boolean dummyEventEnabled = Boolean.parseBoolean(System.getenv().get("EXUI_DUMMY_EVENTS_ENABLED"));
        if (dummyEventEnabled) {
            new PageBuilder(configBuilder
                .event(CASEWORKER_DUMMY_EVENT_EXUI_3839_NO_MID_EVENT)
                .forAllStates()
                .name("EXUI-3839 - No Mid Event")
                .description("EXUI-3839 - No Mid Event")
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER)
                .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
                .page("dummyPage")
                .pageLabel("Dummy Page")
                .optional(CaseData::getDummySetDateAutomatically)
                .mandatory(CaseData::getDummyString, "dummySetDateAutomatically=\"NEVER_SHOW\"", true)
                .done();
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_DUMMY_EVENT_EXUI_3839_NO_MID_EVENT, details.getId());
        CaseData caseData = details.getData();

        log.info("Dummy String is: " + caseData.getDummyString());
        caseData.setDummyString(EXUI_ISSUE_ID);

        log.info("Dummy String is Now: " + caseData.getDummyString());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                        final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_DUMMY_EVENT_EXUI_3839_NO_MID_EVENT, details.getId());
        List<String> errors = new ArrayList<>();

        CaseData caseData = details.getData();
        CaseData beforeCaseData = beforeDetails.getData();
        String dummyString = caseData.getDummyString();
        String beforeDummyString = beforeCaseData.getDummyString();

        String originalDummyString = "Dummy String Was Originally: " + beforeDummyString;
        String currentDummyString = "Dummy String is Now: " + dummyString;
        String expectedDummyString = "Dummy String Should Be: " + EXUI_ISSUE_ID;

        String error = "aboutToSubmit Callback: " + originalDummyString + " " + currentDummyString + " " + expectedDummyString;
        errors.add(error);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }
}
