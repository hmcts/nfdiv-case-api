package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;

public final class GeneralApplicationD11Pages {

    private GeneralApplicationD11Pages() {
    }

    public static void addPages(PageBuilder pageBuilder,
                                TypedPropertyGetter<CaseData, Applicant> primaryApplicant,
                                TypedPropertyGetter<CaseData, Applicant> otherApplicant,
                                String applicant, String pageShowCondition) {

        List<CcdPageConfiguration> pages = List.of(
            new GeneralApplicationD11Page1(primaryApplicant),
            new GeneralApplicationD11Page2(primaryApplicant, applicant),
            new GeneralApplicationD11Page3(primaryApplicant, applicant),
            new GeneralApplicationD11Page4(primaryApplicant),
            new GeneralApplicationD11Page5(primaryApplicant, applicant),
            new GeneralApplicationD11Page6(applicant),
            new GeneralApplicationD11Page7(applicant),
            new GeneralApplicationD11Page8(primaryApplicant, otherApplicant),
            new GeneralApplicationD11Page9(applicant)
        );

        pages.forEach(page -> page.addWithShowCondition(pageBuilder, pageShowCondition));
    }
}
