package uk.gov.hmcts.divorce.solicitor.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.event.CitizenWithdrawGeneralApplication;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DraftApplicationAction;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;
import java.util.OptionalInt;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.findActiveGeneralApplicationIndex;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationDraftSubmissionService {

    private final GeneralApplicationFactory generalApplicationFactory;

    private final GeneralApplicationPaymentPreparationService paymentPreparationService;

    private final CitizenGeneralApplicationSubmissionService submissionService;

    private final CitizenWithdrawGeneralApplication citizenWithdrawGeneralApplication;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

    public void buildGeneralApplication(CaseDetails<CaseData, State> details, CaseData caseData, Applicant applicant) {
        InterimApplicationOptions options = applicant.getInterimApplicationOptions();

        if (options != null
            && options.getDraftApplicationAction() != null
            && DraftApplicationAction.WITHDRAW.equals(options.getDraftApplicationAction())) {

            log.info("Clearing interim options and general application for case id: {}", details.getId());

            OptionalInt genAppIndex = findActiveGeneralApplicationIndex(caseData, applicant);

            if (genAppIndex.isEmpty() && StringUtils.isBlank(applicant.getGeneralAppServiceRequest())) {
                genAppIndex = findHwfDraftIndexForApplicant(caseData, applicant);
            }

            if (genAppIndex.isPresent()) {
                citizenWithdrawGeneralApplication.handleRemovalOfGeneralApplication(caseData, genAppIndex.getAsInt());
            }

            applicant.setInterimApplicationOptions(null);
            return;
        }

        boolean isApplicant1 = isApplicant1(details.getId());

        log.info("Building general application from interim options for case id: {}", details.getId());

        GeneralApplication generalApplication = generalApplicationFactory.createFromJourneyOptions(options, isApplicant1,
            caseData.getApplicationType());
        caseData.setGeneralApplication(generalApplication);

        paymentPreparationService.prepareDraftGeneralApplicationFee(details.getId(), applicant, options, generalApplication);

        DivorceDocument applicationDocument = submissionService.generateGeneralApplicationAnswerDocument(
            details.getId(), applicant, caseData, generalApplication);

        generalApplication.setGeneralApplicationDocument(applicationDocument);
        caseData.updateCaseWithGeneralApplication(generalApplication);
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private OptionalInt findHwfDraftIndexForApplicant(CaseData data, Applicant applicant) {
        List<ListValue<GeneralApplication>> generalApplications = data.getGeneralApplications();
        if (generalApplications == null || generalApplications.isEmpty()) {
            return OptionalInt.empty();
        }

        GeneralParties actingParty =
            applicant == data.getApplicant1() ? GeneralParties.APPLICANT : GeneralParties.RESPONDENT;

        for (int i = 0; i < generalApplications.size(); i++) {
            GeneralApplication ga = generalApplications.get(i).getValue();

            if (ga != null
                && !YesOrNo.YES.equals(ga.getGeneralApplicationSubmittedOnline())
                && actingParty.equals(ga.getGeneralApplicationParty())
                && ga.getGeneralApplicationFee() != null
                && ServicePaymentMethod.FEE_PAY_BY_HWF.equals(ga.getGeneralApplicationFee().getPaymentMethod())) {
                return OptionalInt.of(i);
            }
        }

        return OptionalInt.empty();
    }
}
