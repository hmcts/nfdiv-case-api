package uk.gov.hmcts.divorce.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class ConfirmService {

    public static final String DOCUMENTS_NOT_UPLOADED_ERROR = "Please upload a document in order to continue";

    public List<String> validateConfirmService(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (!isEmpty(caseData.getApplication().getSolicitorService().getServiceProcessedByProcessServer())
            && isEmpty(caseData.getDocuments().getDocumentsUploadedOnConfirmService())) {
            errors.add(DOCUMENTS_NOT_UPLOADED_ERROR);
        }

        return errors;
    }

    public AboutToStartOrSubmitResponse<CaseData, State> getErrorResponse(final CaseDetails<CaseData, State> details,
                                                                          final List<String> validationErrors) {
        log.info("ConfirmService Validation errors: ");
        for (String error : validationErrors) {
            log.info(error);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(validationErrors)
            .state(details.getState())
            .build();
    }

    public void addToDocumentsUploaded(final CaseDetails<CaseData, State> caseDetails) {

        CaseDocuments caseDocuments = caseDetails.getData().getDocuments();

        List<ListValue<DivorceDocument>> documentsUploadedOnConfirmService = caseDocuments.getDocumentsUploadedOnConfirmService();

        if (!isEmpty(documentsUploadedOnConfirmService)) {
            log.info("Adding attachments to documents uploaded.  Case ID: {}", caseDetails.getId());

            if (isNull(caseDocuments.getDocumentsUploaded())) {
                caseDocuments.setDocumentsUploaded(documentsUploadedOnConfirmService);
            } else {
                caseDocuments.getDocumentsUploaded().addAll(documentsUploadedOnConfirmService);
            }

            caseDocuments.setDocumentsUploadedOnConfirmService(null);
        }
    }

}
