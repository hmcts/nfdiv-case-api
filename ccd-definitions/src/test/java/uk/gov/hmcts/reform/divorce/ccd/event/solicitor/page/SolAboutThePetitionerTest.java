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
class SolAboutThePetitionerTest {

    private final FieldCollection.FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolAboutThePetitioner solAboutThePetitioner;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolAboutThePetitionerPageConfiguration() {

        solAboutThePetitioner.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolAboutThePetitioner");
        verify(fieldCollectionBuilder).pageLabel("About the petitioner");
        verify(fieldCollectionBuilder).label(
            "LabelSolAboutEditingApplication-AboutPetitioner",
            "You can make changes at the end of your application.");
        verify(fieldCollectionBuilder).label(
            "LabelSolAboutThePetPara-2",
            "About the petitioner",
            "petitionerNameDifferentToMarriageCertificate=\"Yes\"");
        verify(fieldCollectionBuilder, times(8)).mandatory(any(TypedPropertyGetter.class));
        verify(fieldCollectionBuilder).mandatory(
            any(TypedPropertyGetter.class),
            eq("petitionerNameDifferentToMarriageCertificate=\"Yes\""));
        verify(fieldCollectionBuilder).mandatory(
            any(TypedPropertyGetter.class),
            eq("petitionerNameChangedHow=\"other\""));
        verify(fieldCollectionBuilder, times(2)).optional(any(TypedPropertyGetter.class));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
