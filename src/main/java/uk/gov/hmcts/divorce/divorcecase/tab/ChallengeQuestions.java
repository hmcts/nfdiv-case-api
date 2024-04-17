//package uk.gov.hmcts.divorce.divorcecase.tab;
//
//import org.springframework.stereotype.Component;
//import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
//import uk.gov.hmcts.ccd.sdk.api.ChallengeQuestion;
//import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
//import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
//import uk.gov.hmcts.divorce.divorcecase.model.State;
//import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
//
//@Component
//public class ChallengeQuestions implements CCDConfig<CaseData, State, UserRole> {
//
//    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
//        List<ChallengeQuestion> questions = new ArrayList<>();
//        questions.add(ChallengeQuestion.builder()
//            .displayOrder(1)
//            .questionText("Enter the Case Reference").answer("${marriageDate}").questionId("caseRef").build());
//        questions.add(ChallengeQuestion.builder()
//            .displayOrder(2)
//            .questionText("Enter marriage date in YYYY-MM-DD format eg. 2011-03-08 ")
//            .answer("${applicant1LastName}").questionId("lastName").build());
//        configBuilder.challengeQuestions(questions);
//    }
//}
