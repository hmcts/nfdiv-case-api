package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.REQUEST_FOR_INFORMATION;

@Component
@RequiredArgsConstructor
public class RequestForInformationDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_REQUEST_FOR_INFORMATION = "request-for-information-letter";

    private static final DocumentPackInfo APPLICANT_REQUEST_FOR_INFORMATION_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION, Optional.of(REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo SOLICITOR_REQUEST_FOR_INFORMATION_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION, Optional.of(REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(final CaseData caseData, final Applicant applicant) {
        final boolean isApplicantRepresented = applicant.isRepresented();

        return isApplicantRepresented ? SOLICITOR_REQUEST_FOR_INFORMATION_PACK : APPLICANT_REQUEST_FOR_INFORMATION_PACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_REQUEST_FOR_INFORMATION;
    }
}
