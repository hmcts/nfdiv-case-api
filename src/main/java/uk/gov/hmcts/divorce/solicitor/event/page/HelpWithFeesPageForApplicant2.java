package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;

@Slf4j
@Component
public class HelpWithFeesPageForApplicant2 implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant2NeedsHelpWithFees=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("HelpWithFeesPageForApplicant2")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .showCondition("applicationType=\"soleApplication\"")
            .pageLabel("Help with fees")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant2NeedsHelpWithFees)
                .complex(Application::getApplicant2HelpWithFees)
                    .mandatory(HelpWithFees::getReferenceNumber,
                "applicant2NeedsHelpWithFees=\"Yes\"",
                null,
                "Applicant 2 help with fees reference")
                .done()
            .done();
    }
}
