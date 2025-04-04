package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Map;

@Component
@Slf4j
public class SetFailedMigrationVersionToZero implements CaseTask {

    @Autowired
    private ObjectMapper objectMapper;

    public static final int HIGHEST_PRIORITY = 0;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        Map<String, Object> mappedData = objectMapper.convertValue(caseDetails.getData(), new TypeReference<>() {});
        mappedData.put("dataVersion", HIGHEST_PRIORITY);

        final var convertedData = objectMapper.convertValue(mappedData, CaseData.class);
        caseDetails.setData(convertedData);

        return caseDetails;
    }
}
