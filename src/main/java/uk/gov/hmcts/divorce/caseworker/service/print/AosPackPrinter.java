package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.print.BulkPrintService;
import uk.gov.hmcts.divorce.print.model.Print;

import java.util.Collections;

@Component
@Slf4j
public class AosPackPrinter {

    @Autowired
    private BulkPrintService bulkPrintService;

    public void print(final CaseData caseData, final Long caseId) {

        final Print print = new Print(Collections.emptyList(), caseId.toString(), "", "");
        bulkPrintService.print(print);

    }
}
