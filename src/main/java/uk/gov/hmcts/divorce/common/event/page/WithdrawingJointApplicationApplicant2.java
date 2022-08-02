package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

import static uk.gov.hmcts.divorce.common.event.page.WithdrawingJointApplicationApplicant1.getFirstInTimeLabel;
import static uk.gov.hmcts.divorce.common.event.page.WithdrawingJointApplicationApplicant1.getSecondInTimeLabel;
import static uk.gov.hmcts.divorce.common.event.page.WithdrawingJointApplicationApplicant1.getWithdrawLabel;

public class WithdrawingJointApplicationApplicant2 implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "coApplicant2ApplyForConditionalOrder=\"NEVER_SHOW\"";

    @SuppressWarnings("checkstyle:magicnumber")
    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("WithdrawingJointApplicationFirstApp2")
            .pageLabel("Withdrawing a joint application")
            .showCondition("coApplicant2ApplyForConditionalOrder=\"No\"")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getDivorceOrEndingCivilPartnership, NEVER_SHOW)
            .done()
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                    .readonlyNoSummary(ConditionalOrderQuestions::getConfirmInformationStillCorrect, NEVER_SHOW)
                .done()
            .done()
            .label("withdrawLabelApp2", getWithdrawLabel())
            .label("firstInTimeApp2", getFirstInTimeLabel(), "coApplicant1ConfirmInformationStillCorrect!=\"*\"")
            .label("secondInTimeApp2", getSecondInTimeLabel(), "coApplicant1ConfirmInformationStillCorrect=\"*\"")
            .done()
        ;
    }
}
