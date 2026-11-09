package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorAmendD8bApplicationActionPage implements CcdPageConfiguration {

    public static final String ACTION_LABEL = "Do you want to amend your draft request to submit D8B Answer or withdraw it?";

    public static final String ACTION_HINT = "If you withdraw it, you’ll be able to start a new D8B Answer application.";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("amendGeneralApplication")
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getInterimApplicationOptions)
                    .mandatory(InterimApplicationOptions::getDraftApplicationAction, ALWAYS_SHOW, null, ACTION_LABEL,
                        ACTION_HINT)
                .done()
            .done();
    }
}
