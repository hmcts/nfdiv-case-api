package uk.gov.hmcts.divorce.sow014.lib;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.util.Map;

@Data
public class POCCaseEvent {

    private Map<String, Object> caseDetails;
    private Map<String, Object> caseDetailsBefore;
    private POCEventDetails eventDetails;

    @JsonCreator
    public POCCaseEvent(Map<String, Object> caseDetails, POCEventDetails eventDetails,
                        Map<String, Object> caseDetailsBefore) {
        this.caseDetailsBefore = caseDetailsBefore;
        this.caseDetails = caseDetails;
        this.eventDetails = eventDetails;
    }
}
