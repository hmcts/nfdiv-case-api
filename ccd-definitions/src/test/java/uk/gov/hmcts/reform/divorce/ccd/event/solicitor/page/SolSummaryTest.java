package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
public class SolSummaryTest {

    private final FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolSummary solSummary;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolSummaryPageConfiguration() {

        solSummary.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolSummary");
        verify(fieldCollectionBuilder).label("LabelSolAppSummaryPara-1", "# Before you submit");
        verify(fieldCollectionBuilder).label("LabelSolAppSummaryPara-2", "## What happens next");
        verify(fieldCollectionBuilder).label(
            "LabelSolAppSummaryPara-3",
            "### Please continue to submit your application on the next screen. \r\nThe application "
                + "will be checked. If it’s correct, you’ll be sent a notice of issue. The respondent will "
                + "also receive a copy of the application unless you have chosen to personally effect service.");
        verify(fieldCollectionBuilder).label(
            "LabelSolAppSummaryPara-4",
            "In cases of adultery where the co-respondent was named, they will also get a copy and a "
                + "form to return.");
        verify(fieldCollectionBuilder).label(
            "LabelSolAppSummaryPara-5",
            "Contact the divorce centre if you don't hear anything back after 3 weeks.");
        verify(fieldCollectionBuilder).label(
            "LabelSolAppSummaryPara-6",
            "Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)\r\nEmail: contactdivorce@justice.gov.uk");
        verify(fieldCollectionBuilder).label("LabelSolAppSummaryPara-7", "## Help us improve this service");
        verify(fieldCollectionBuilder).label(
            "LabelSolAppSummaryPara-8",
            "This is a new service that is still being developed. If you haven't already done so, "
                + "please provide feedback on what you think of it and how it can be improved.");
        verify(fieldCollectionBuilder).label("LabelSolAppSummaryPara-9", "## If you need help");
        verify(fieldCollectionBuilder).label(
            "LabelSolAppSummaryPara-10",
            "You can contact the divorce centre if you need help with your application.");
        verify(fieldCollectionBuilder).label(
            "LabelSolAppSummaryPara-11",
            "Phone: 0300 303 0642 (Monday to Friday, 8.30am to 5pm)\r\nEmail: contactdivorce@justice.gov.uk");

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
