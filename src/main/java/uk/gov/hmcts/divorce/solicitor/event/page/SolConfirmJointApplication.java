package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class SolConfirmJointApplication implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("ConfirmJointApplication")
            .pageLabel("Confirm Joint Application")
            .showCondition("applicationType=\"jointApplication\"");
        // link to the PDF document that was generated (with all the information) to go here
        // document generation will be part of another ticket
    }
}
