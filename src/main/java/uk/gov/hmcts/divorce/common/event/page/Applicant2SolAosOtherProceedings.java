package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class Applicant2SolAosOtherProceedings implements CcdPageConfiguration {

    private static final String OTHER_PROCEEDINGS_NO_DOCUMENT_ERROR = "Please upload a document in order to continue";
    private static final String LEGAL_PROCEEDINGS_CONCLUDED = "You will have to upload evidence to show that the proceedings have been "
        + "concluded or withdrawn. For example, an order or confirmation email from the court that the proceedings have been concluded "
        + "or withdrawn.";
    private static final String LEGAL_PROCEEDINGS_ONGOING = "You will have to upload evidence to show that the proceedings are ongoing. "
        + "For example, an email or notice from the court that the proceedings are ongoing.";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolAosOtherProceedings", this::midEvent)
            .pageLabel("Other legal proceedings")
            .complex(CaseData::getApplicant2)
            .mandatory(Applicant::getLegalProceedings)
            .mandatory(Applicant::getLegalProceedingsDetails, "applicant2LegalProceedings=\"Yes\"")
            .mandatory(Applicant::getLegalProceedingsConcluded, "applicant2LegalProceedings=\"Yes\"")
            .label("applicant2LegalProceedingsConcludedLabel", LEGAL_PROCEEDINGS_CONCLUDED,
                "applicant2LegalProceedingsConcluded=\"Yes\"")
            .label("applicant2LegalProceedingsOngoingLabel", LEGAL_PROCEEDINGS_ONGOING,
                "applicant2LegalProceedingsConcluded=\"No\"")
            .optional(Applicant::getLegalProceedingDocs,
                "applicant2LegalProceedings=\"Yes\" AND applicant2LegalProceedingsConcluded=\"*\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final Applicant respondent = caseData.getApplicant2();

        if (isEmpty(respondent.getLegalProceedingDocs())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(OTHER_PROCEEDINGS_NO_DOCUMENT_ERROR))
                .build();
        }

        List<String> documentErrors =
            validateUploadedDocumentsForMandatoryFields(respondent.getLegalProceedingDocs());

        if (!documentErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(documentErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private List<String> validateUploadedDocumentsForMandatoryFields(List<ListValue<DivorceDocument>> docs) {
        List<String> errors = new ArrayList<>();

        if (docs.stream()
            .map(ListValue::getValue)
            .anyMatch(doc -> isNull(doc.getDocumentLink()))) {
            errors.add("No document attached to one or more uploads");
        }

        return errors;
    }
}
