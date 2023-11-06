package uk.gov.hmcts.divorce.document.print.documentpack;

import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

/** An implementation of the DocumentPack interface should include all the potential variations of documents that may be sent
 * depending on case data variables, represented as a
 * {@link  DocumentPackInfo}.
 *
 * <p>Ensure that the DocumentType map is an ordered map which maintains insertion order. You can use this property to order the letter
 * pack when you instantiate the data.
 *
 * <p>The easiest way to create an ordered map statically is
 * Google Guava's {@link com.google.common.collect.ImmutableMap#of()} method.
 *
 * <p>For example implementation see {@link ConditionalOrderRefusalDocumentPack}
 */
public interface DocumentPack {
    /*** Contains all the business logic required to determine which pack to generate and send for a given applicant and case.
     * @param caseData - case data
     * @param applicant - the applicant to whom document pack is being sent
     * @return - the document pack to generate
     */
    DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant);

    String getLetterId();
}
