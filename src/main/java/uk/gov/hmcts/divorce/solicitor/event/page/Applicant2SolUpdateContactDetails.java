package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;

@Component
@Slf4j
@RequiredArgsConstructor
public class Applicant2SolUpdateContactDetails implements CcdPageConfiguration {

    private final Applicant1SolUpdateContactDetails applicant1SolUpdateContactDetails;

    private static final String APP2_SOL_UPDATE_CONTACT_DETAILS_PAGE = "Applicant2SolUpdateContactDetails";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page(APP2_SOL_UPDATE_CONTACT_DETAILS_PAGE, this::midEvent)
            .pageLabel("Update your contact details")
            .complex(CaseData::getApplicant2)
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
        log.info("{} Mid-event callback invoked for Case Id: {}", APP2_SOL_UPDATE_CONTACT_DETAILS_PAGE, details.getId());

        return applicant1SolUpdateContactDetails.solicitorEmailValidationResponse(
            details.getData().getApplicant2().getSolicitor(), details
        );
    }
}
