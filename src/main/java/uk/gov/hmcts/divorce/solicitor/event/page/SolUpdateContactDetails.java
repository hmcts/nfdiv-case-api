package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class SolUpdateContactDetails implements CcdPageConfiguration {

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
                    .mandatory(Solicitor::getName, null, null, "Your name")
                    .mandatory(Solicitor::getPhone, null, null, "Your phone number")
                    .mandatory(Solicitor::getEmail, null, null, "Your email")
                    .mandatory(Solicitor::getAgreeToReceiveEmails)
                    .complex(Solicitor::getOrganisationPolicy)
                        .complex(OrganisationPolicy::getOrganisation)
                            .mandatory(Organisation::getOrganisationId)
                        .done()
                    .done()
                .done()
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for SolUpdateContactDetails");
        return solicitorCreateApplicationService.validateSolicitorOrganisation(
            details.getData(),
            details.getId(),
            request.getHeader(AUTHORIZATION)
        );
    }
}
