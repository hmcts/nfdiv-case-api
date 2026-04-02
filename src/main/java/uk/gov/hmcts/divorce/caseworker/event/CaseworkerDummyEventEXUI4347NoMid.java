package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
public class CaseworkerDummyEventEXUI4347NoMid implements CCDConfig<CaseData, State, UserRole> {

    public static final String EVENT_NAME = "caseworker-dummy-event-exui4347-no-mid-event";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(EVENT_NAME)
            .forAllStates()
            .name("EXUI-4347 - No Mid Event")
            .description("EXUI-4347 - No Mid Event")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
            .page("dummyPage")
            .pageLabel("Dummy Page")
            .optional(CaseData::getDummySetDateAutomatically)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", EVENT_NAME, details.getId());
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
        log.info("{} about to submit callback invoked for Case Id: {}", EVENT_NAME, details.getId());
        List<String> errors = new ArrayList<>();
        String alwaysThrowError = "Always Throw Error To Preserve Test Case Data";
        errors.add(alwaysThrowError);

        CaseData caseData = details.getData();
        CaseData beforeCaseData = beforeDetails.getData();
        String dummyString = caseData.getDummyString();
        String beforeDummyString = beforeCaseData.getDummyString();

        String originalDummyString = "Dummy String Was Originally: " + beforeDummyString;
        String currentDummyString = "Dummy String is Now: " + dummyString;
        String expectedDummyString = "Dummy String Should Be: " + EXUI_ISSUE_ID;

        String error = "aboutToSubmit Callback: " + originalDummyString + " " + currentDummyString + " " + expectedDummyString;
        if (!EXUI_ISSUE_ID.equals(dummyString)) {
            errors.add(error);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }
}
