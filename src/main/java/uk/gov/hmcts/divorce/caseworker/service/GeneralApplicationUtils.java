package uk.gov.hmcts.divorce.caseworker.service;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class GeneralApplicationUtils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm:ss a");

    private GeneralApplicationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void populateGeneralApplicationList(CaseData caseData) {
        List<DynamicListElement> generalApplicationNames = generalApplicationLabels(caseData)
            .values().stream().map(label -> DynamicListElement
                .builder()
                .label(label)
                .code(UUID.randomUUID())
                .build()
            ).toList();

        caseData.getGeneralApplication().setGeneralApplicationToReject(
            DynamicList.builder()
                .listItems(generalApplicationNames)
                .build()
        );
    }

    public static Map<Integer, String> generalApplicationLabels(CaseData data) {
        List<ListValue<GeneralApplication>> generalApplications = data.getGeneralApplications();

        if (CollectionUtils.isEmpty(generalApplications)) {
            return Collections.emptyMap();
        }

        return IntStream.range(0, generalApplications.size())
            .filter(idx -> generalApplications.get(idx).getValue() != null)
            .boxed()
            .collect(Collectors.toMap(
                idx -> idx, idx -> generalApplications.get(idx).getValue().getLabel(idx, formatter)
            ));
    }
}
