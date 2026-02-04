package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.SeparationOrderGranted;

@Slf4j
@Component
@RequiredArgsConstructor
public class PronounceCasesTask implements BulkCaseTask {

    final EnumSet<State> awaitingPronouncement = EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived);
    final EnumSet<State> postStates = EnumSet.of(ConditionalOrderPronounced, SeparationOrderGranted);

    private final AuthTokenGenerator authTokenGenerator;

    private final IdamService idamService;

    private final BulkCaseTaskUtil bulkCaseTaskUtil;

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
