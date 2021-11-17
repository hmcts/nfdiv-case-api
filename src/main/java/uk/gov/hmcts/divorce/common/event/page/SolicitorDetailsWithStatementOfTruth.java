package uk.gov.hmcts.divorce.common.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;

@Component
public class SolicitorDetailsWithStatementOfTruth implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SubmitAos")
            .pageLabel("Statement of Truth - Solicitor")
            .complex(CaseData::getAcknowledgementOfService)
                .label("labelApplicant2SolStatementOfTruth-SOT", "## Statement of truth")
                .mandatory(AcknowledgementOfService::getStatementOfTruth)
                .done()
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .complex(Solicitor::getOrganisationPolicy, null, "Your firm's address or DX number")
                        .complex(OrganisationPolicy::getOrganisation)
                            .mandatory(Organisation::getOrganisationId)
                            .done()
                    .done()
                .done()
            .done()
            .complex(CaseData::getAcknowledgementOfService)
                .optional(AcknowledgementOfService::getAdditionalComments)
            .done()
            .label("warning-ProceedingForContent",
                "*Proceedings for contempt of court may be brought against anyone who makes, or causes to be made, "
                    + "a false statement verified by a statement of truth without an honest belief in its truth *");
    }
}
