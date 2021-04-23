package uk.gov.hmcts.divorce.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageBuilderTest {

    @Mock
    private EventBuilder<CaseData, UserRole, State> eventBuilder;

    @InjectMocks
    private PageBuilder pageBuilder;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldReturnFieldCollectionBuilderForPageWithId() {

        final FieldCollectionBuilder fieldCollectionBuilder = mock(FieldCollectionBuilder.class);

        when(eventBuilder.fields()).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.page("id")).thenReturn(fieldCollectionBuilder);

        assertThat(pageBuilder.page("id"), is(fieldCollectionBuilder));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldReturnFieldCollectionBuilderForPageWithIdAndMidEventCallback() {

        final FieldCollectionBuilder fieldCollectionBuilder = mock(FieldCollectionBuilder.class);
        final MidEvent midEvent = mock(MidEvent.class);

        when(eventBuilder.fields()).thenReturn(fieldCollectionBuilder);
        when(fieldCollectionBuilder.page("id", midEvent)).thenReturn(fieldCollectionBuilder);

        assertThat(pageBuilder.page("id", midEvent), is(fieldCollectionBuilder));
    }
}