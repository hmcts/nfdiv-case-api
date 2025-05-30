package uk.gov.hmcts.divorce.systemupdate.convert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseDetailsListConverter {

    private final CaseDetailsConverter caseDetailsConverter;

    public List<CaseDetails<CaseData, State>> convertToListOfValidCaseDetails(
        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetailsList) {

        return caseDetailsList.stream()
            .map(caseDetails -> {
                try {
                    return caseDetailsConverter.convertToCaseDetailsFromReformModel(caseDetails);
                } catch (final IllegalArgumentException e) {
                    log.info(
                        "Case failed to deserialize, removing from search results. Case ID: {}",
                        caseDetails.getId());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
