package uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.function.Predicate;

@Component
@Slf4j
public class CaseCourtHearingPredicate {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    public Predicate<ListValue<BulkListCaseDetails>> caseHearingIsNotSet(final BulkActionCaseData bulkActionCaseData,
                                                                         final User user,
                                                                         final String serviceAuthorization) {
        return listValue -> {
            final var caseReference = listValue.getValue().getCaseReference().getCaseReference();

            final var conditionalOrder = caseDetailsConverter
                .convertToCaseDetailsFromReformModel(coreCaseDataApi
                    .getCase(user.getAuthToken(), serviceAuthorization, caseReference))
                .getData()
                .getConditionalOrder();

            return !bulkActionCaseData.getDateAndTimeOfHearing().equals(conditionalOrder.getDateAndTimeOfHearing())
                || !bulkActionCaseData.getCourt().equals(conditionalOrder.getCourt());
        };
    }
}
