package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class AmendGeneralApplicationActionPage implements CcdPageConfiguration {

    private final TypedPropertyGetter<CaseData, Applicant> applicantRef;

    public static final String ACTION_LABEL = "Do you want to amend your draft general application or withdraw it?";

    public static final String ACTION_HINT = "If you withdraw it, you’ll be able to start a new general application.";

    public AmendGeneralApplicationActionPage(TypedPropertyGetter<CaseData, Applicant> applicantRef) {
        this.applicantRef = applicantRef;
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("amendGeneralApplication")
            .complex(applicantRef)
                .complex(Applicant::getInterimApplicationOptions)
                    .mandatory(InterimApplicationOptions::getDraftApplicationAction, ALWAYS_SHOW, null, ACTION_LABEL,
                        ACTION_HINT)
                .done()
            .done();
    }
}
