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
import uk.gov.hmcts.divorce.divorcecase.model.DummyFields;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerDummyEventEXUI4347SetData implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_DUMMY_EVENT_EXUI_4347_SET_DATA = "caseworker-dummy-event-exui4347-set-data";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final boolean dummyEventEnabled = Boolean.parseBoolean(System.getenv().get("EXUI_DUMMY_EVENTS_ENABLED"));
        if (dummyEventEnabled) {
            new PageBuilder(configBuilder
                .event(CASEWORKER_DUMMY_EVENT_EXUI_4347_SET_DATA)
                .forAllStates()
                .name("EXUI-3839-4347 Set Dummy Data")
                .description("EXUI-3839-4347 Set Dummy Data")
                .aboutToSubmitCallback(this::aboutToSubmit)
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER)
                .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
                .page("dummyPage")
                .pageLabel("Dummy Page - Set Dummy Data")
                .complex(CaseData::getExuiDummyFields)
                    .optional(DummyFields::getDummyDate)
                    .optional(DummyFields::getDummyEnumField)
                    .optional(DummyFields::getNullAllDummyFields)
                .done()
                .done();
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_DUMMY_EVENT_EXUI_4347_SET_DATA, details.getId());

        if (YesOrNo.YES.equals(details.getData().getExuiDummyFields().getNullAllDummyFields())) {
            log.info("Nulling all dummy fields");
            details.getData().setExuiDummyFields(new DummyFields());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
