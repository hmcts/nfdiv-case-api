package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
public class SolStatementOfTruthTest {

    private final FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolStatementOfTruth solStatementOfTruth;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolStatementOfTruthPageConfiguration() {

        solStatementOfTruth.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolStatementOfTruth");
        verify(fieldCollectionBuilder).pageLabel("Statement of truth and reconciliation");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOfTruthPara-1",
            "## The petitioner is applying to the court");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOfTruthPara-1.1",
            "That the marriage be dissolved as it has broken down irretrievably.");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOfTruthPara-1.2",
            "That a costs order may be granted.");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOfTruthPara-1.3",
            "That a financial order may be granted.");
        verify(fieldCollectionBuilder, times(8))
            .mandatory(any(TypedPropertyGetter.class));
        verify(fieldCollectionBuilder)
            .optional(any(TypedPropertyGetter.class), eq("SolUrgentCase=\"Yes\""));
        verify(fieldCollectionBuilder, times(2))
            .mandatoryNoSummary(any(TypedPropertyGetter.class), eq("D8StatementOfTruth=\"Banana\""));
        verify(fieldCollectionBuilder).label("LabelSolServiceMethod", "## Service method");
        verify(fieldCollectionBuilder).label(
            "LabelSolPersonalService",
            "After service is complete you must notify the court by completing the 'Confirm Service' form "
                + "in CCD. Refer to the information pack for further instruction on how to do this",
            "SolServiceMethod=\"personalService\"");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOTruthPara-3",
            "## Statement of reconciliation");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOfTruthPara-2",
            "## Statement of truth");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOTruthPara-7",
            "You could be fined or imprisoned for contempt of court if you deliberately submit false information.");
        verify(fieldCollectionBuilder).label(
            "LabelSolStatementOTruthPara-8",
            "If you have any comments you would like to make to the court staff regarding the application "
                + "you may include them below.");
        verify(fieldCollectionBuilder).optionalNoSummary(any(TypedPropertyGetter.class));
        verify(fieldCollectionBuilder)
            .readonlyNoSummary(any(TypedPropertyGetter.class));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
