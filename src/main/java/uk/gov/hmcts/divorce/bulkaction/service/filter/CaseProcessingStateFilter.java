package uk.gov.hmcts.divorce.bulkaction.service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseProcessingStateFilter {

    private final CcdSearchService ccdSearchService;

    private final ObjectMapper objectMapper;

    public CaseFilterProcessingState filterProcessingState(final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
                                                           final User user,
                                                           final String serviceAuth,
                                                           final EnumSet<State> prestates,
                                                           final EnumSet<State> postStates) {

        final List<String> processedCaseReferences = new ArrayList<>();
        final List<String> erroredCaseReferences = new ArrayList<>();
        final List<ListValue<BulkListCaseDetails>> processableCases = new ArrayList<>();
        final List<ListValue<BulkListCaseDetails>> erroredCases = new ArrayList<>();
        final List<ListValue<BulkListCaseDetails>> processedCases = new ArrayList<>();

        ccdSearchService.searchForCases(getCasesReferences(bulkListCaseDetails), user, serviceAuth)
            .forEach(caseDetails -> {
                State state = State.valueOf(caseDetails.getState());
                CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

                var caseIsAlreadyPronounced = !isEmpty(caseData.getFinalOrder())
                        && !isEmpty(caseData.getFinalOrder().getDateFinalOrderEligibleFrom());

                if (postStates.contains(state) || hasFinalOrder(caseData)
                        || (state.equals(State.OfflineDocumentReceived) && caseIsAlreadyPronounced)) {

                    log.info(
                        "Case ID {} will be skipped and moved to processed list as already processed",
                        caseDetails.getId());
                    processedCaseReferences.add(String.valueOf(caseDetails.getId()));
                } else if (isValidPrestate(prestates, caseDetails)) {
                    log.info("Case ID {} will be added to unprocessedCases", caseDetails.getId());
                } else {
                    log.info(
                        "Case ID {} will be skipped and moved to error list as not in correct state to be processed",
                        caseDetails.getId());
                    erroredCaseReferences.add(String.valueOf(caseDetails.getId()));
                }
            });

        bulkListCaseDetails.forEach(bulkCase -> {
            if (processedCaseReferences.contains(bulkCase.getValue().getCaseReference().getCaseReference())) {
                processedCases.add(bulkCase);
            } else if (erroredCaseReferences.contains(bulkCase.getValue().getCaseReference().getCaseReference())) {
                erroredCases.add(bulkCase);
            } else {
                processableCases.add(bulkCase);
            }
        });

        return new CaseFilterProcessingState(processableCases, erroredCases, processedCases);
    }

    private boolean isValidPrestate(final EnumSet<State> startStates, final CaseDetails caseDetails) {
        return startStates.contains(State.valueOf(caseDetails.getState()));
    }

    private List<String> getCasesReferences(final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails) {
        return bulkListCaseDetails.stream()
            .map(bulkCase -> bulkCase.getValue().getCaseReference().getCaseReference())
            .toList();
    }

    private boolean hasFinalOrder(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getFinalOrder)
            .map(FinalOrder::getGrantedDate)
            .isPresent();
    }
}
