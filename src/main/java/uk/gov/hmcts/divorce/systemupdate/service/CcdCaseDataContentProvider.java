package uk.gov.hmcts.divorce.systemupdate.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

@Component
public class CcdCaseDataContentProvider {

    public CaseDataContent createCaseDataContent(final StartEventResponse startEventResponse,
                                                 final String summary,
                                                 final String description,
                                                 final Object data) {


        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(summary)
                    .description(description)
                    .build())
            .data(data)
            .build();
    }
}
