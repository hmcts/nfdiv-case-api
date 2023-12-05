package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.util.BulkCaseTaskUtil;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InBulkActionCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;

@Slf4j
@Component
public class PronounceCasesTask implements BulkCaseTask {

    // TODO NFDIV-3824: We've introduced a new state (InBulkActionCase) for cases which are linked to bulk lists. They will no longer
    //  use the AwaitingPronouncement state. AwaitingPronouncement state should be removed once all cases using this state
    //  (pre- new state InBulkActionCase) have been pronounced and final order granted.
    final EnumSet<State> awaitingPronouncement = EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived, InBulkActionCase);
    final EnumSet<State> postStates = EnumSet.of(ConditionalOrderPronounced);

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    @Autowired
    BulkCaseTaskUtil bulkCaseTaskUtil;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> details) {

        return bulkCaseTaskUtil.pronounceCases(
                details,
                awaitingPronouncement,
                postStates,
                idamService.retrieveSystemUpdateUserDetails(),
                authTokenGenerator.generate());
    }
}
