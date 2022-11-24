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

    private static final String APPLICANT_1_ALREADY_APPLIED_FOR_FO_ERROR_MESSAGE = "Applicant 1 has already applied for final order.";
    private static final String APPLICANT_2_ALREADY_APPLIED_FOR_FO_ERROR_MESSAGE = "Applicant 2 has already applied for final order.";

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

    public static List<String> validateApplyForFinalOrder(final CaseData caseData, boolean isApplicant2Event) {
        final var finalOrder = caseData.getFinalOrder();
        final List<String> errors = new ArrayList<>();

        if (YES.equals(finalOrder.getApplicant1AppliedForFinalOrderFirst()) && !isApplicant2Event) {
            errors.add(APPLICANT_1_ALREADY_APPLIED_FOR_FO_ERROR_MESSAGE);
        }

        if (YES.equals(finalOrder.getApplicant2AppliedForFinalOrderFirst()) && isApplicant2Event) {
            errors.add(APPLICANT_2_ALREADY_APPLIED_FOR_FO_ERROR_MESSAGE);
        }

        return errors;
    }
}
