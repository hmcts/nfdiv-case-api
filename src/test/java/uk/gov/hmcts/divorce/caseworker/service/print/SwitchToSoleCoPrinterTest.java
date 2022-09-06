package uk.gov.hmcts.divorce.caseworker.service.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.Clock;

@ExtendWith(MockitoExtension.class)
public class SwitchToSoleCoPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private SwitchToSoleCoPrinter printer;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldGenerateAndPrintSwitchToSoleCoLetter() {

    }

    @Test
    void shouldNotPrintSwitchToSoleCoLetterIfRequiredDocumentNotPresent() {

    }
}
