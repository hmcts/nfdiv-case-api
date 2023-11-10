package uk.gov.hmcts.divorce.document;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentGenerationPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;

@RequiredArgsConstructor
@Component
public class DocumentGenerationUtil {

    private DocumentGenerator documentGenerator;

    public void generateDocuments(CaseDetails<CaseData, State> details, DocumentPack documentPack, List<DocumentType> docsToRemove) {

        CaseData caseData = details.getData();

        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                    .removeIf(document -> docsToRemove.contains(document.getValue().getDocumentType()));
        }

        var applicant1 = caseData.getApplicant1();
        var applicant2 = caseData.getApplicant2();

        Map.of(applicant1, applicant1.isApplicantOffline(),
                        applicant2, applicant2.isApplicantOffline() || isBlank(caseData.getApplicant2EmailAddress()))
                .entrySet().stream().filter(Map.Entry::getValue)
                .forEach(applicant -> documentGenerator.generateDocuments(caseData, details.getId(), applicant.getKey(),
                        documentPack.getDocumentPack()));
    }

    public void generate(DocumentGenerationPack documentGenerationPack, CaseDetails<CaseData, State> details, DocumentPack documentPack,
                         List<DocumentType> docsToRemove) {
        documentGenerationPack.generateDocuments.generate(this, details, documentPack, docsToRemove);
    }

    public static DocumentPackInfo getDocumentPack(ImmutableMap<DocumentType, Optional<String>> documentPack,
                                                   ImmutableMap<String, String> templateInfo,
                                                   String letterId) {
        return new DocumentPackInfo(documentPack, templateInfo, letterId);
    }
}
