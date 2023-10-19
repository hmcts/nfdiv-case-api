package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetSolicitorTemplateContent;
import uk.gov.hmcts.divorce.document.content.JudicialSeparationCoRefusalTemplateContent;
import uk.gov.hmcts.divorce.legaladvisor.service.task.CoRefusalTemplateContent;

import java.time.Clock;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderRefusedDocumentGeneratorTest {

    @Mock
    private CoversheetSolicitorTemplateContent coversheetSolicitorTemplateContent;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Mock
    private JudicialSeparationCoRefusalTemplateContent judicialSeparationCoRefusalTemplateContent;

    @Mock
    private CoRefusalTemplateContent coRefusalTemplateContent;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ConditionalOrderRefusedDocumentGenerator conditionalOrderRefusedDocumentGenerator;

    @Test
    void shouldGenerateAwaitingAmendedApplicationPack() {

    }

    @Test
    void shouldGenerateAwaitingClarificationPack() {

    }

    @Test
    void shouldGenerateAwaitingAmendedApplicationPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingClarificationApplicationPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingAmendedApplicationSolicitorPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingClarificationApplicationSolicitorPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingAmendedApplicationPackForSeparationOrder() {

    }

    @Test
    void shouldGenerateAwaitingClarificationPackForSeparationOrder() {

    }
}
