package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DraftServiceApplicationAction;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceApplicationDraftSubmissionService {

    private final ServiceApplicationFactory serviceApplicationFactory;
    private final ServiceApplicationPaymentPreparationService paymentPreparationService;
    private final InterimApplicationSubmissionService interimApplicationSubmissionService;

    public void submitFromInterimOptions(long caseId, CaseData caseData, Applicant applicant) {
        InterimApplicationOptions options = applicant.getInterimApplicationOptions();

        if (null != options.getDraftServiceApplicationAction()
            && DraftServiceApplicationAction.WITHDRAW.equals(options.getDraftServiceApplicationAction())) {

            log.info("clearing the interim options and Alternative Service for case id: {}", caseId);

            applicant.setInterimApplicationOptions(null);
            caseData.setAlternativeService(null);
            return;
        }

        log.info(
            "Building service application from interim options for case id: {}", caseId);

        AlternativeService serviceApplication = serviceApplicationFactory.createFromInterimOptions(options);

        caseData.setAlternativeService(serviceApplication);

        paymentPreparationService.prepareDraftServiceApplicationFee(caseId, applicant, options, serviceApplication);

        DivorceDocument applicationDocument = interimApplicationSubmissionService
            .generateServiceApplicationAnswerDocument(caseId, applicant, caseData);
        serviceApplication.setServiceApplicationAnswers(applicationDocument);
    }
}
