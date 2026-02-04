package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.REQUEST_FOR_INFORMATION_RESPONSE;

@Component
@RequiredArgsConstructor
public class RequestForInformationResponseDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_REQUEST_FOR_INFORMATION_RESPONSE = "request-for-information-response-letter";

    private static final DocumentPackInfo APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_RESPONSE, Optional.of(REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_RESPONSE, Optional.of(REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(final CaseData caseData, final Applicant applicant) {
        return applicant.isRepresented()
            ? SOLICITOR_REQUEST_FOR_INFORMATION_RESPONSE_PACK
            : APPLICANT_REQUEST_FOR_INFORMATION_RESPONSE_PACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_REQUEST_FOR_INFORMATION_RESPONSE;
    }
}
