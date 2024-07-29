package uk.gov.hmcts.divorce.solicitor.event.page;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class Applicant1SolUpdateContactDetails implements CcdPageConfiguration {

    @Autowired
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Autowired
    private HttpServletRequest request;

    private static final String INVALID_EMAIL_ERROR = "Please enter an email address that is linked to your organisation";
    private static final String APP1_SOL_UPDATE_CONTACT_DETAILS_PAGE = "Applicant1SolUpdateContactDetails";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page(APP1_SOL_UPDATE_CONTACT_DETAILS_PAGE, this::midEvent)
            .pageLabel("Update your contact details")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .mandatoryWithLabel(Solicitor::getName, "Your name")
                    .mandatoryWithLabel(Solicitor::getPhone, "Your phone number")
                    .mandatoryWithLabel(Solicitor::getEmail, "Your email")
                    .mandatory(Solicitor::getAgreeToReceiveEmailsCheckbox)
                    .mandatoryWithLabel(Solicitor::getAddress, "Firm address")
                    .mandatory(Solicitor::getAddressOverseas)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        log.info("{} Mid-event callback invoked for Case Id: {}", APP1_SOL_UPDATE_CONTACT_DETAILS_PAGE, details.getId());

        return solicitorEmailValidationResponse(details.getData().getApplicant1().getSolicitor(), details);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> solicitorEmailValidationResponse(
        Solicitor solicitor, CaseDetails<CaseData, State> details
    ) {
        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisationAndEmail(
            solicitor, details.getId(), request.getHeader(AUTHORIZATION)
        );

        if (caseInfo.getErrors() != null && !caseInfo.getErrors().isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(INVALID_EMAIL_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .build();
    }
}
