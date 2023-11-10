package uk.gov.hmcts.divorce.document.print.documentpack;

import uk.gov.hmcts.divorce.document.DocumentGenerationUtil;

public interface DocumentPackGenerator<T, R,
            S, U > {
        void generate(T docs, R details, S documentPack, U docsToRemove);
}
