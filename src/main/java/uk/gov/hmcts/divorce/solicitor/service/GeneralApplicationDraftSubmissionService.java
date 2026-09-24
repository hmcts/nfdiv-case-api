package uk.gov.hmcts.divorce.solicitor.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationDraftSubmissionService {

    private final GeneralApplicationFactory generalApplicationFactory;

    private final GeneralApplicationPaymentPreparationService paymentPreparationService;

    private final CitizenGeneralApplicationSubmissionService submissionService;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

    public void buildGeneralApplication(CaseDetails<CaseData, State> details, CaseData caseData, Applicant applicant) {
        InterimApplicationOptions options = applicant.getInterimApplicationOptions();

        boolean isApplicant1 = isApplicant1(details.getId());

        log.info("Building general application from interim options for case id: {}", details.getId());

        GeneralApplication generalApplication = generalApplicationFactory.createFromJourneyOptions(options, isApplicant1,
            caseData.getApplicationType());
        caseData.setGeneralApplication(generalApplication);

        paymentPreparationService.prepareDraftGeneralApplicationFee(details.getId(), applicant, options, generalApplication);

        DivorceDocument applicationDocument = submissionService.generateGeneralApplicationAnswerDocument(
            details.getId(), applicant, caseData, generalApplication);

        generalApplication.setGeneralApplicationDocument(applicationDocument);
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }
}
