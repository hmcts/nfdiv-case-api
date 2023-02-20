package uk.gov.hmcts.divorce.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.ProgressApplicant1FinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.ProgressApplicant2FinalOrderState;
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

    private static final String APP1_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE = "Applicant / Applicant 1 has already applied for final order.";
    private static final String APP2_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE = "Applicant 2 has already applied for final order.";

    @Autowired
    private SetFinalOrderFieldsAsApplicant1 setFinalOrderFieldsAsApplicant1;

    @Autowired
    private SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    @Autowired
    private ProgressApplicant1FinalOrderState progressApplicant1FinalOrderState;


    @Autowired
    private ProgressApplicant2FinalOrderState progressApplicant2FinalOrderState;

    public CaseDetails<CaseData, State> applyForFinalOrderAsApplicant1(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant1,
            progressApplicant1FinalOrderState
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> applyForFinalOrderAsApplicant2(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant2,
            progressApplicant2FinalOrderState
        ).run(caseDetails);
    }

    public List<String> validateApplyForFinalOrder(final CaseData caseData, boolean isApplicant2Event) {
        final var finalOrder = caseData.getFinalOrder();
        final List<String> errors = new ArrayList<>();

        if (YES.equals(finalOrder.getApplicant1AppliedForFinalOrderFirst()) && !isApplicant2Event) {
            errors.add(APP1_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE);
        }

        if (YES.equals(finalOrder.getApplicant2AppliedForFinalOrderFirst()) && isApplicant2Event) {
            errors.add(APP2_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE);
        }

        return errors;
    }
}
