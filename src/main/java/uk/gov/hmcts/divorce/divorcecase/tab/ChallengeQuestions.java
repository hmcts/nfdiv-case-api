package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.ChallengeQuestion;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;

@Component
public class ChallengeQuestions implements CCDConfig<CaseData, State, UserRole> {

    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.challengeQuestions(List.of(
            ChallengeQuestion.builder()
                .displayOrder(1)
                .questionText("Enter the Marriage Date")
                .answer("${marriageDate}")
                .answerFieldType("Date")
                .questionId("marriageDate")
                .caseTypeID(getCaseType()).build(),
            ChallengeQuestion.builder()
                .displayOrder(2)
                .questionText("Enter Applicant 1s Last Name")
                .answer("${applicant1LastName}")
                .questionId("applicant1LastName")
                .caseTypeID(getCaseType()).build()
        ));
    }
}
