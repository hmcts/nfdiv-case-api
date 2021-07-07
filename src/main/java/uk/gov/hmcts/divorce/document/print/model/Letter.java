package uk.gov.hmcts.divorce.document.print.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

@AllArgsConstructor
@Getter
public class Letter {
    private final DivorceDocument divorceDocument;
    private final int count;
}
