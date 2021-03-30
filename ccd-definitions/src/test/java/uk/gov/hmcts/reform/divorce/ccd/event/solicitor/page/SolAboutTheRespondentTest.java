package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
class SolAboutTheRespondentTest {

    private final FieldCollection.FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolAboutTheRespondent solAboutTheRespondent;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolAboutTheRespondentPageConfiguration() {

        solAboutTheRespondent.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolAboutTheRespondent");
        verify(fieldCollectionBuilder).pageLabel("About the respondent");
        verify(fieldCollectionBuilder).label(
            "LabelSolAboutEditingApplication-AboutRespondent",
            "You can make changes at the end of your application.");
        verify(fieldCollectionBuilder, times(4)).mandatory(any(TypedPropertyGetter.class));
        verify(fieldCollectionBuilder).optional(
            any(TypedPropertyGetter.class),
            eq("respondentNameAsOnMarriageCertificate=\"Yes\""));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
