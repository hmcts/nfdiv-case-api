package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MigrateRetiredFields implements CaseTask {

    private final ObjectMapper objectMapper;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) throws IllegalArgumentException  {

        Map<String, Object> mappedData = objectMapper.convertValue(caseDetails.getData(), new TypeReference<>() {});
        final var migratedMappedData = RetiredFields.migrate(mappedData);

        final var convertedData = objectMapper.convertValue(migratedMappedData, CaseData.class);
        caseDetails.setData(convertedData);

        return caseDetails;
    }
}
