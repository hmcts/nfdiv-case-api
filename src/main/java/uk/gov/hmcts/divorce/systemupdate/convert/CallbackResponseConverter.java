package uk.gov.hmcts.divorce.systemupdate.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

@Component
@Slf4j
public class CallbackResponseConverter {

    @Autowired
    private ObjectMapper objectMapper;

    public AboutToStartOrSubmitResponse<CaseData, State> convertResponseFromCcdModel(AboutToStartOrSubmitCallbackResponse response) {
        log.info("About to start converting ccd response\n {}", response);
        return objectMapper.convertValue(response, new TypeReference<>() {});
    }
}
