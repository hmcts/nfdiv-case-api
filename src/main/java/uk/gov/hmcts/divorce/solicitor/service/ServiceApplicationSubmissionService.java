package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

@Service
@RequiredArgsConstructor
public class ServiceApplicationSubmissionService {

    private final ServiceApplicationFactory serviceApplicationFactory;
    private final ServiceApplicationPaymentPreparationService paymentPreparationService;
    private final InterimApplicationSubmissionService interimApplicationSubmissionService;

    public void submitFromInterimOptions(long caseId, CaseData caseData, Applicant applicant) {
        InterimApplicationOptions options = applicant.getInterimApplicationOptions();
        AlternativeService serviceApplication = serviceApplicationFactory.createFromInterimOptions(options);

        caseData.setAlternativeService(serviceApplication);

        paymentPreparationService.prepareDraftServiceApplicationFee(caseId, applicant, options, serviceApplication);

        DivorceDocument applicationDocument = interimApplicationSubmissionService
                .generateServiceApplicationAnswerDocument(caseId, applicant, caseData);
        serviceApplication.setServiceApplicationAnswers(applicationDocument);

        applicant.archiveInterimApplicationOptions();
    }
}
