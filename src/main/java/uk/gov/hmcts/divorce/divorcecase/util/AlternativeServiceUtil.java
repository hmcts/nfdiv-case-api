package uk.gov.hmcts.divorce.divorcecase.util;

import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.CollectionUtils.isEmpty;

public final class AlternativeServiceUtil {

    private AlternativeServiceUtil(){
    }

    public static void archiveAlternativeServiceApplicationOnCompletion(CaseData caseData) {

        AlternativeService alternativeService = caseData.getAlternativeService();

        if (null != alternativeService) {

            alternativeService.setReceivedServiceAddedDate(LocalDate.now());

            if (isEmpty(caseData.getAlternativeServiceApplications())) {

                List<ListValue<AlternativeService>> listValues = new ArrayList<>();

                var listValue = ListValue
                    .<AlternativeService>builder()
                    .id("1")
                    .value(alternativeService)
                    .build();

                listValues.add(listValue);
                caseData.setAlternativeServiceApplications(listValues);

            } else {

                AtomicInteger listValueIndex = new AtomicInteger(0);
                var listValue = ListValue
                    .<AlternativeService>builder()
                    .value(alternativeService)
                    .build();

                caseData.getAlternativeServiceApplications().add(0, listValue);
                caseData.getAlternativeServiceApplications().forEach(applicationListValue ->
                    applicationListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
            }
            // Null the current AlternativeService object instance in the CaseData so that a new one can be created
            caseData.setAlternativeService(null);
        }
    }
}
