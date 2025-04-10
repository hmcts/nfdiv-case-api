package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SetFailedBulkCaseMigrationVersionToZero implements BulkCaseTask {

    private final ObjectMapper objectMapper;

    public static final int HIGHEST_PRIORITY = 0;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails)
        throws IllegalArgumentException  {

        Map<String, Object> mappedData = objectMapper.convertValue(caseDetails.getData(), new TypeReference<>() {});
        mappedData.put("bulkCaseDataVersion", HIGHEST_PRIORITY);

        final BulkActionCaseData convertedData = objectMapper.convertValue(mappedData, BulkActionCaseData.class);
        caseDetails.setData(convertedData);

        return caseDetails;
    }
}
