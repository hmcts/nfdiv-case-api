package uk.gov.hmcts.divorce.sow014.nfd;

import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import static org.jooq.impl.DSL.upper;
import static org.jooq.nfdiv.public_.Tables.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import uk.gov.hmcts.divorce.sow014.lib.DynamicRadioListElement;
import uk.gov.hmcts.divorce.sow014.lib.MyRadioList;

@Component
@Slf4j
public class ChangeLeadCase implements CCDConfig<CaseData, State, UserRole> {
    @Autowired
    @Lazy
    private DSLContext db;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event("change-lead-case")
            .forAllStates()
            .name("Change lead case")
            .description("Change lead case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showCondition("leadCase=\"Yes\"")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grant(CREATE_READ_UPDATE_DELETE,
                SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
            .page("changeLeadCase", this::searchCases)
            .pageLabel("Change the lead case")
            .mandatory(CaseData::getCaseSearchTerm)
            .page("Choose the new lead case")
            .mandatory(CaseData::getCaseSearchResults);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> searchCases(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {

        var choices = new ArrayList<DynamicRadioListElement>();
        // Search subcases by applicantFirstName
        db.selectFrom(SUB_CASES)
            .where( SUB_CASES.LEAD_CASE_ID.eq(details.getId())
                    .and(upper(SUB_CASES.APPLICANT1FIRSTNAME).eq(upper(details.getData().getCaseSearchTerm())))
            )
            .limit(100)
            .fetch()
            .forEach(fetch -> choices.add(DynamicRadioListElement.builder()
                .code(fetch.getSubCaseId().toString())
                .label(fetch.getSubCaseId() + " - " + fetch.getApplicant1firstname() + " - " + fetch.getApplicant1lastname())
                .build()));

        MyRadioList radioList = MyRadioList.builder()
            .value(choices.get(0))
            .listItems(choices)
            .build();

        details.getData().setCaseSearchResults(radioList);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        var choice = details.getData().getCaseSearchResults().getValue();

        // Make the chosen case the lead case.
        db.update(MULTIPLES)
            .set(MULTIPLES.LEAD_CASE_ID, Long.parseLong(choice.getCode()))
            .where(MULTIPLES.LEAD_CASE_ID.eq(details.getId()))
            .execute();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}

