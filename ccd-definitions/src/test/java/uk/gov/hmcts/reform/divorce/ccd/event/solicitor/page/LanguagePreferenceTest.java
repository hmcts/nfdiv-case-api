package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
public class LanguagePreferenceTest {
    private final FieldCollection.FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private LanguagePreference languagePreference;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddLanguagePreferencePageConfiguration() {

        languagePreference.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("langPref");
        verify(fieldCollectionBuilder).pageLabel("Select Language");
        verify(fieldCollectionBuilder).mandatory(any(TypedPropertyGetter.class));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
