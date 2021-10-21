package uk.gov.hmcts.divorce.solicitor.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class SolConfirmService implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("SolConfirmService")
            .pageLabel("Certificate of Service - Confirm Service")
            .label("petitionerLabel", "Name of Applicant - ${applicant1FirstName} ${applicant1LastName}")
            .label("respondentLabel", "Name of Respondent - ${applicant2FirstName} ${applicant2LastName}")
            .complex(CaseData::getApplication)
                .complex(Application::getSolicitorService)
                .mandatory(SolicitorService::getDateOfService)
                .mandatory(SolicitorService::getDocumentsServed)
                .mandatory(SolicitorService::getOnWhomServed)
                .mandatory(SolicitorService::getHowServed)
                .mandatory(SolicitorService::getServiceDetails, "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
                .mandatory(SolicitorService::getAddressServed)
                .mandatory(SolicitorService::getBeingThe)
                .mandatory(SolicitorService::getLocationServed)
                .mandatory(SolicitorService::getSpecifyLocationServed, "solServiceLocationServed=\"otherSpecify\"")
                .mandatory(SolicitorService::getServiceSotName)
                .readonly(SolicitorService::getTruthStatement)
                .mandatory(SolicitorService::getServiceSotFirm)
                .done();
    }
}
