package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class SolUpdateContactDetails implements CcdPageConfiguration {

    private static final String INVALID_EMAIL_ERROR = "You have entered an invalid email address. "
        + "Please check the email and enter it again, before submitting the application.";

    @Autowired
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolUpdateContactDetails",this::midEvent)
            .pageLabel("Update contact details")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .mandatoryWithLabel(Solicitor::getName, "Your name")
                    .mandatoryWithLabel(Solicitor::getPhone, "Your phone number")
                    .mandatoryWithLabel(Solicitor::getEmail, "Your email")
                    .mandatory(Solicitor::getAgreeToReceiveEmailsCheckbox)
                    .complex(Solicitor::getOrganisationPolicy)
                        .complex(OrganisationPolicy::getOrganisation)
                            .mandatory(Organisation::getOrganisationId)
                        .done()
                    .done()
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        log.info("Mid-event callback triggered for SolUpdateContactDetails");

        CaseData caseData = details.getData();
        Applicant applicant1 = caseData.getApplicant1();
        List<String> validationErrors = new ArrayList<>();

        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisation(
            caseData,
            details.getId(),
            request.getHeader(AUTHORIZATION)
        );

        if (caseInfo.getErrors() != null) {
            validationErrors.addAll(caseInfo.getErrors());
        }

        boolean validEmail = EmailValidator.getInstance().isValid(applicant1.getSolicitor().getEmail());
        if (!validEmail) {
            validationErrors.add(INVALID_EMAIL_ERROR);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(validationErrors)
            .build();
    }
}
