package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Hearing;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingDate;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingOutcome;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerListForHearing implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_LIST_FOR_HEARING = "caseworker-list-for-hearing";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_LIST_FOR_HEARING)
            .forStateTransition(
                PendingHearingDate, PendingHearingOutcome
            )
            .name("List for hearing")
            .description("List for hearing")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
            .page("listForHearing")
            .pageLabel("List for hearing")
            .complex(CaseData::getHearing)
            .mandatory(Hearing::getDateOfHearing)
            .mandatory(Hearing::getVenueOfHearing)
            .mandatory(Hearing::getHearingAttendance)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_LIST_FOR_HEARING, details.getId());

        var caseData = details.getData();

        final Hearing copyOfHearing = caseData.getHearing().toBuilder().build();

        final ListValue<Hearing> hearingListValue = ListValue.<Hearing>builder()
            .id(UUID.randomUUID().toString())
            .value(copyOfHearing)
            .build();

        if (isNull(caseData.getHearings())) {
            caseData.setHearings(singletonList(hearingListValue));
        } else {
            caseData.getHearings().add(0, hearingListValue);
        }

        caseData.setHearing(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
