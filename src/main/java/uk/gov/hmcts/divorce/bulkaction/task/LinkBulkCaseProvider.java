package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;

@Component
@Slf4j
public class LinkBulkCaseProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_LINK_WITH_BULK_CASE;
    }

    @Override
    public CaseTask getCaseTask(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {
        return mainCaseDetails -> {
            log.info("Updating case data for Case Id: {} Event: {}", mainCaseDetails.getId(), getEventId());
            mainCaseDetails.getData().setBulkListCaseReferenceLink(
                CaseLink
                    .builder()
                    .caseReference(String.valueOf(bulkCaseDetails.getId()))
                    .build()
            );
            return mainCaseDetails;
        };
    }
}
