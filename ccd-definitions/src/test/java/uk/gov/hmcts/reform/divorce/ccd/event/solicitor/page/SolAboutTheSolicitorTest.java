package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
class SolAboutTheSolicitorTest {

    private final FieldCollection.FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolAboutTheSolicitor solAboutTheSolicitor;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolAboutTheSolicitorPageConfiguration() {

        solAboutTheSolicitor.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolAboutTheSolicitor");
        verify(fieldCollectionBuilder).pageLabel("About the Solicitor");
        verify(fieldCollectionBuilder).label(
            "LabelSolAboutEditingApplication-AboutSolicitor",
            "You can make changes at the end of your application.");
        verify(fieldCollectionBuilder).label(
            "LabelSolAboutTheSolPara-1",
            "Please note that the information provided will be used as evidence by the court to decide if "
                + "the petitioner is entitled to legally end their marriage. **A copy of this form is sent to the "
                + "respondent**");
        verify(fieldCollectionBuilder, times(6)).mandatory(any(TypedPropertyGetter.class));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
