package uk.gov.hmcts.divorce.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.ProgressFinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant1;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Service
public class ApplyForFinalOrderService {

    @Autowired
    private SetFinalOrderFieldsAsApplicant1 setFinalOrderFieldsAsApplicant1;

    @Autowired
    private SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    @Autowired
    private ProgressFinalOrderState progressFinalOrderState;

    public CaseDetails<CaseData, State> applyForFinalOrderAsApplicant1(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant1,
            progressFinalOrderState
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> applyForFinalOrderAsApplicant2(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant2,
            progressFinalOrderState
        ).run(caseDetails);
    }

    public static List<String> validateApplyForFinalOrder(final CaseData caseData, boolean isApplicant1) {
        final var finalOrder = caseData.getFinalOrder();
        final List<String> errors = new ArrayList<>();

       if (!caseData.getApplicationType().isSole() && YES.equals(finalOrder.getApplicant1AppliedForFinalOrderFirst()) && isApplicant1) {
            errors.add("Applicant 1 has already applied for final order.");
        }

        if (!caseData.getApplicationType().isSole() && YES.equals(finalOrder.getApplicant2AppliedForFinalOrderFirst()) && !isApplicant1) {
            errors.add("Applicant 2 has already applied for final order.");
        }

        return errors;
    }
}
