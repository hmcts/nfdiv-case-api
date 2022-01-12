package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

@Component
@Slf4j
public class SolicitorFirmDetailsApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolicitorFirmDetailsApplicant2")
            .showCondition("applicant2ConfirmApplicant1Information=\"Yes\"")
            .pageLabel("Statement of truth - Solicitor")
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName)
                    .mandatory(Solicitor::getFirm)
                    .optional(Solicitor::getAdditionalComments)
                .done()
            .done();
    }
}
