package uk.gov.hmcts.divorce.solicitor.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;

@Component
public class AddLastAlternativeServiceDocumentLink implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();

        getAlternativeServiceDocumentLink(caseData)
            .ifPresentOrElse(
                document -> caseData.getConditionalOrder().setLastAlternativeServiceDocumentLink(document),
                () -> caseData.getConditionalOrder().setLastAlternativeServiceDocumentLink(null));

        return caseDetails;
    }

    private Optional<Document> getAlternativeServiceDocumentLink(final CaseData caseData) {

        return caseData.getFirstAlternativeServiceOutcome()
            .flatMap(alternativeServiceOutcome -> {
                final AlternativeServiceType alternativeServiceType = alternativeServiceOutcome.getAlternativeServiceType();
                final CaseDocuments documents = caseData.getDocuments();

                if (alternativeServiceType == BAILIFF && alternativeServiceOutcome.hasBeenSuccessfullyServedByBailiff()) {
                    return alternativeServiceOutcome.getCertificateOfServiceDocumentLink();
                } else if (alternativeServiceOutcome.hasServiceApplicationBeenGranted() && nonNull(documents)) {

                    if (alternativeServiceType == DEEMED) {
                        return documents.getFirstGeneratedDocumentLinkWith(DEEMED_AS_SERVICE_GRANTED);
                    } else if (alternativeServiceType == DISPENSED) {
                        return documents.getFirstGeneratedDocumentLinkWith(DISPENSE_WITH_SERVICE_GRANTED);
                    }
                }

                return empty();
            });
    }
}
