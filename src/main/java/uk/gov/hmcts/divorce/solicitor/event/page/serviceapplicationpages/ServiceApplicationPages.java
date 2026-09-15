package uk.gov.hmcts.divorce.solicitor.event.page.serviceapplicationpages;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

import java.util.List;

public final class ServiceApplicationPages {

    private ServiceApplicationPages() {
    }

    public static void addAlternativeServicePages(final PageBuilder pageBuilder, final String pageShowCondition) {
        final List<CcdPageConfiguration> pages = List.of(
            new AlternativeServiceConfirmPage(),
            new AlternativeServicePaymentPage(),
            new AlternativeServiceReasonPage(),
            new AlternativeServiceMethodPage(),
            new AlternativeServiceAbleToUploadEvidence(),
            new AlternativeServiceDetailsAndUploadPage()
        );

        pages.forEach(page -> page.addWithShowCondition(pageBuilder, pageShowCondition));
    }
}
