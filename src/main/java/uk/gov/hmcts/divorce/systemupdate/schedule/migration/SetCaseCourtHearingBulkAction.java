package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.CaseTriggerService;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate.CaseCourtHearingPredicate;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.CaseCourtHearingTaskProvider;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.function.Predicate;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCaseWithCoEGeneration.SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION;

@Slf4j
@Component
public class SetCaseCourtHearingBulkAction {

    @Autowired
    private CaseCourtHearingPredicate caseCourtHearingPredicate;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseTriggerService caseTriggerService;

    @Autowired
    private CaseCourtHearingTaskProvider caseCourtHearingTask;

    public void setCaseCourtHearing(final CaseDetails bulkCaseDetails, final User user, final String serviceAuthorization) {

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkActionDetails
            = caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(bulkCaseDetails);

        final BulkActionCaseData bulkActionCaseData = bulkActionDetails.getData();
        final Predicate<ListValue<BulkListCaseDetails>> caseHearingIsNotSet = caseCourtHearingPredicate
            .caseHearingIsNotSet(bulkActionCaseData, user, serviceAuthorization);

        final CaseTask caseTask = caseCourtHearingTask.getCaseTask(bulkActionDetails);

        bulkActionCaseData.getBulkListCaseDetails().stream()
            .filter(caseHearingIsNotSet)
            .forEach(listValue -> {
                caseTriggerService.caseTrigger(
                    listValue,
                    SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION,
                    caseTask,
                    user,
                    serviceAuthorization);
            });
    }
}
