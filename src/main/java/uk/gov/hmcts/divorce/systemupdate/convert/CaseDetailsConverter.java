package uk.gov.hmcts.divorce.systemupdate.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Component
public class CaseDetailsConverter {

    @Autowired
    private ObjectMapper objectMapper;

    public CaseDetails convertToReformModel(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails) {
        return objectMapper.convertValue(caseDetails, CaseDetails.class);
    }
}
