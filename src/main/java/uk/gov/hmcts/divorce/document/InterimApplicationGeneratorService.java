package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;

@Service
@RequiredArgsConstructor
public class InterimApplicationGeneratorService {
  private final DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;

  public DivorceDocument generateAnswerDocument(
    long caseId,
    Applicant applicant,
    CaseData caseData
  ) {
    GeneralApplicationType applicationType = applicant.getInterimApplicationOptions().getInterimApplicationType();

    if (GeneralApplicationType.DEEMED_SERVICE.equals(applicationType)) {
      return deemedServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
    }

    return null;
  }
}
