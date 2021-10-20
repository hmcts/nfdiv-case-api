package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ListValueUtil;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.service.task.PronounceCase;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

@Component
@Slf4j
public class CaseworkerPronounceList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_PRONOUNCE_LIST = "caseworker-pronounce-list";

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private PronounceCase pronounceCase;

    @Autowired
    private ListValueUtil listValueUtil;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_PRONOUNCE_LIST)
            .forStateTransition(Listed, Pronounced)
            .name("Conditional Order Pronounced")
            .description("Conditional Order Pronounced")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(CaseDetails<BulkActionCaseData, BulkActionState> details,
                                                                                           CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails) {

        final BulkActionCaseData caseData = details.getData();

        caseData.setPronouncedDate(caseData.getDateAndTimeOfHearing().toLocalDate());
        caseData.setDateFinalOrderEligibleFrom(caseData.getDateAndTimeOfHearing().toLocalDate().plusWeeks(6).plusDays(1));

        caseData.setBulkListCaseDetails(
            listValueUtil.fromListToListValue(
                bulkTriggerService.bulkTrigger(
                    listValueUtil.fromListValueToList(caseData.getBulkListCaseDetails()),
                    SYSTEM_PRONOUNCE_CASE,
                    pronounceCase,
                    idamService.retrieveUser(request.getHeader(AUTHORIZATION)),
                    authTokenGenerator.generate()))
        );

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .state(Pronounced)
            .build();
    }
}

