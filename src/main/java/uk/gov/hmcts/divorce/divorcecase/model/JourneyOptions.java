package uk.gov.hmcts.divorce.divorcecase.model;

import uk.gov.hmcts.divorce.document.model.DivorceDocument;

public interface JourneyOptions {
    boolean citizenWillMakePayment();
    String citizenHwfReference();
    DivorceDocument generateAnswerDocument();
    AlternativeServiceType serviceApplicationType();
}
