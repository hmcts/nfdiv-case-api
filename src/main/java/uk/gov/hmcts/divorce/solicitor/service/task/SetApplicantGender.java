package uk.gov.hmcts.divorce.solicitor.service.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;

@Component
public class SetApplicantGender implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        var data = details.getData();
        Gender app1Gender;
        Gender app2Gender;
        WhoDivorcing whoDivorcing;

        if (data.getDivorceOrDissolution().isDivorce()) {
            // for a divorce we ask who is applicant1 divorcing to infer applicant2's gender, then use the marriage
            // formation to infer applicant 1's gender
            whoDivorcing = data.getApplication().getDivorceWho();
            app2Gender = whoDivorcing == HUSBAND ? MALE : FEMALE;
            app1Gender = data.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app2Gender);
        } else {
            // for a dissolution we ask for applicant1's gender and use the marriage formation to infer applicant 2's
            // gender and who they are divorcing
            app1Gender = data.getApplicant1().getGender();
            app2Gender = data.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app1Gender);
            whoDivorcing = app2Gender == MALE ? HUSBAND : WIFE;
        }

        data.getApplicant1().setGender(app1Gender);
        data.getApplicant2().setGender(app2Gender);
        data.getApplication().setDivorceWho(whoDivorcing);

        return details;
    }
}
