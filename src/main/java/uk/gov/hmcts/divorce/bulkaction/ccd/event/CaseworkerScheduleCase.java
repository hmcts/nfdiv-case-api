package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ListValueUtil;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCaseErrors.SYSTEM_BULK_CASE_ERRORS;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

@Component
@Slf4j
public class CaseworkerScheduleCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CASEWORKER_SCHEDULE_CASE = "caseworker-schedule-case";

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private ListValueUtil listValueUtil;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_SCHEDULE_CASE)
            .forStateTransition(Created, Listed)
            .name("Schedule cases for listing")
            .description("Schedule cases for listing")
            .showSummary()
            .showEventNotes()
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("scheduleForListing")
            .pageLabel("Schedule cases for listing")
            .mandatory(BulkActionCaseData::getCourtName)
            .mandatory(BulkActionCaseData::getDateAndTimeOfHearing)
            .mandatoryNoSummary(BulkActionCaseData::getBulkListCaseDetails);
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        final BulkActionCaseData bulkActionCaseData = bulkCaseDetails.getData();
        final List<ListValue<BulkListCaseDetails>> originalBulkListCaseDetails = bulkActionCaseData.getBulkListCaseDetails();

        bulkActionCaseData.setBulkListCaseDetails(
            listValueUtil.fromListToListValue(
                bulkTriggerService.bulkTrigger(
                    listValueUtil.fromListValueToList(bulkActionCaseData.getBulkListCaseDetails()),
                    SYSTEM_UPDATE_CASE_COURT_HEARING,
                    mainCaseDetails -> {
                        final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
                        conditionalOrder.setDateAndTimeOfHearing(
                            bulkCaseDetails.getData().getDateAndTimeOfHearing()
                        );
                        conditionalOrder.setCourtName(
                            bulkCaseDetails.getData().getCourtName()
                        );
                        return mainCaseDetails;
                    },
                    idamService.retrieveUser(request.getHeader(AUTHORIZATION)),
                    authTokenGenerator.generate()))
        );

        appendFailedCasesToErrorList(
            bulkActionCaseData,
            listValueUtil.fromListValueToList(originalBulkListCaseDetails));

        try {
            ccdUpdateService.submitBulkActionEvent(
                bulkCaseDetails,
                SYSTEM_BULK_CASE_ERRORS,
                idamService.retrieveUser(request.getHeader(AUTHORIZATION)),
                authTokenGenerator.generate()
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {} ", bulkCaseDetails.getId());
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private void appendFailedCasesToErrorList(BulkActionCaseData caseData, List<BulkListCaseDetails> originalCaseList) {
        List<Long> updatedCaseList = listValueUtil.fromListValueToList(caseData.getBulkListCaseDetails())
            .stream()
            .map(c -> Long.valueOf(c.getCaseReference().getCaseReference()))
            .collect(toList());

        List<BulkListCaseDetails> errorCases = originalCaseList
            .stream()
            .filter(c -> !updatedCaseList.contains(Long.valueOf(c.getCaseReference().getCaseReference())))
            .collect(toList());

        caseData.getErroredCaseDetails().addAll(listValueUtil.fromListToListValue(errorCases));
    }
}
