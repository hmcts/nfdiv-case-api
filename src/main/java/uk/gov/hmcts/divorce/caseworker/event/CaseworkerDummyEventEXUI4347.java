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

import java.time.LocalDate;
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
public class CaseworkerDummyEventEXUI4347 implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_DUMMY_EVENT_EXUI_4347 = "caseworker-dummy-event-exui4347";
    
    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final boolean dummyEventEnabled = Boolean.parseBoolean(System.getenv().get("EXUI_DUMMY_EVENTS_ENABLED"));
        if (dummyEventEnabled) {
            new PageBuilder(configBuilder
                .event(CASEWORKER_DUMMY_EVENT_EXUI_4347)
                .forAllStates()
                .name("EXUI-3839-4347")
                .description("EXUI-3839-4347")
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER)
                .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
                .page("dummyPage", this::midEvent)
                .pageLabel("Dummy Page")
                .complex(CaseData::getExuiDummyFields)
                    .mandatory(DummyFields::getDummySetDateAutomatically)
                    .mandatory(DummyFields::getDummyDate, "dummySetDateAutomatically=\"NEVER_SHOW\"", true)
                    .mandatory(DummyFields::getDummyEnumField, "dummySetDateAutomatically=\"NEVER_SHOW\"", true)
                .done()
                .done();
        }
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_DUMMY_EVENT_EXUI_4347, details.getId());
        DummyFields dummyFields = details.getData().getExuiDummyFields();

        log.info("Dummy Enum Field is: " + dummyFields.getDummyEnumField());
        dummyFields.setDummyEnumField(DummyFields.DummyEnum.DUMMY_ENUM_1);

        log.info("Dummy Enum Field is Now: " + dummyFields.getDummyEnumField());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} midEvent callback invoked for Case Id: {}", CASEWORKER_DUMMY_EVENT_EXUI_4347, details.getId());

        DummyFields dummyFields = details.getData().getExuiDummyFields();
        DummyFields beforeDummyFields = beforeDetails.getData().getExuiDummyFields();
        List<String> warnings = new ArrayList<>();

        DummyFields.DummyEnum dummyEnumField = dummyFields.getDummyEnumField();
        DummyFields.DummyEnum beforeDummyEnumField = beforeDummyFields.getDummyEnumField();

        String originalDummyEnum = "Dummy Enum Field Was Originally: " + beforeDummyEnumField;
        String currentDummyEnum = "Dummy Enum Field is Now: " + dummyEnumField;
        String expectedDummyEnum = "Dummy Enum Field Should Be: " + DummyFields.DummyEnum.DUMMY_ENUM_1;

        warnings.add("aboutToSubmit Callback: " + originalDummyEnum + " " + currentDummyEnum + " " + expectedDummyEnum);

        if (YesOrNo.YES.equals(dummyFields.getDummySetDateAutomatically())) {
            log.info("Dummy Date is: " + dummyFields.getDummyDate());
            dummyFields.setDummyDate(LocalDate.now());
            log.info("Dummy Date is Now: " + dummyFields.getDummyDate());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .warnings(warnings)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                        final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_DUMMY_EVENT_EXUI_4347, details.getId());
        List<String> errors = new ArrayList<>();

        DummyFields dummyFields = details.getData().getExuiDummyFields();
        DummyFields beforeDummyFields = beforeDetails.getData().getExuiDummyFields();
        DummyFields.DummyEnum dummyEnumField = dummyFields.getDummyEnumField();
        DummyFields.DummyEnum beforeDummyEnumField = beforeDummyFields.getDummyEnumField();

        String originalDummyEnum = "Dummy Enum Field Was Originally: " + beforeDummyEnumField;
        String currentDummyEnum = "Dummy Enum Field is Now: " + dummyEnumField;
        String expectedDummyEnum = "Dummy Enum Field Should Be: " + DummyFields.DummyEnum.DUMMY_ENUM_1;

        errors.add("aboutToSubmit Callback: " + originalDummyEnum + " " + currentDummyEnum + " " + expectedDummyEnum);

        if (YesOrNo.YES.equals(dummyFields.getDummySetDateAutomatically())) {
            String originalDummyDate = "Dummy Date Was Originally: " + beforeDummyFields.getDummyDate();
            String currentDummyDate = "Dummy Date is Now: " + dummyFields.getDummyDate();
            String expectedDummyDate = "Dummy Date Should Be: " + LocalDate.now();
            errors.add("aboutToSubmit Callback: " + originalDummyDate + " " + currentDummyDate + " " + expectedDummyDate);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(errors)
            .build();
    }
}
