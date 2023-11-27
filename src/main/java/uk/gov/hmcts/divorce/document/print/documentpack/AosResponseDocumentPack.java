package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class AosResponseDocumentPack implements DocumentPack {

    private static final DocumentPackInfo RESPONDENT_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(

        ),
        ImmutableMap.of()
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {
        return null;
    }

    @Override
    public String getLetterId() {
        return null;
    }
}
