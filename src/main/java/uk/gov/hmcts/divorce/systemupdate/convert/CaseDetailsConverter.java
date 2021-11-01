package uk.gov.hmcts.divorce.systemupdate.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Component
public class CaseDetailsConverter {

    @Autowired
    private ObjectMapper objectMapper;

    public CaseDetails convertToReformModelFromCaseDetails(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails) {
        return objectMapper.convertValue(caseDetails, CaseDetails.class);
    }

    public uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> convertToCaseDetailsFromReformModel(final CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails, new TypeReference<>() {
        });
    }

    public CaseDetails convertToReformModelFromBulkActionCaseDetails(
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails) {

        return objectMapper.convertValue(caseDetails, CaseDetails.class);
    }

    public uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> convertToBulkActionCaseDetailsFromReformModel(
        final CaseDetails caseDetails) {

        return objectMapper.convertValue(caseDetails, new TypeReference<>() {
        });
    }
}
