package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkCaseRetiredFields;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;

import java.util.Map;

@Component
@Slf4j
public class MigrateBulkCaseRetiredFields implements BulkCaseTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails)
        throws IllegalArgumentException  {

        Map<String, Object> mappedData = objectMapper.convertValue(caseDetails.getData(), new TypeReference<>() {});
        final Map<String, Object> migratedMappedData = BulkCaseRetiredFields.migrate(mappedData);

        final BulkActionCaseData convertedData = objectMapper.convertValue(migratedMappedData, BulkActionCaseData.class);
        caseDetails.setData(convertedData);

        return caseDetails;
    }
}
