package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

public class WithdrawingJointApplicationApplicant1 implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "coApplicant1ApplyForConditionalOrder=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("WithdrawingJointApplicationFirstApp1")
            .pageLabel("Withdrawing a joint application")
            .showCondition("coApplicant1ApplyForConditionalOrder=\"No\"")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getDivorceOrEndingCivilPartnership, NEVER_SHOW)
            .done()
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant2Questions)
                    .readonlyNoSummary(ConditionalOrderQuestions::getConfirmInformationStillCorrect, NEVER_SHOW)
                .done()
            .done()
            .label("withdrawLabelApp1", getWithdrawLabel())
            .label("firstInTimeApp1", getFirstInTimeLabel(), "coApplicant2ConfirmInformationStillCorrect!=\"*\"")
            .label("secondInTimeApp1", getSecondInTimeLabel(), "coApplicant2ConfirmInformationStillCorrect=\"*\"")
            .done();
    }

    public static String getWithdrawLabel() {
        return
            "If you want to withdraw this joint application then both parties need to jointly fill out a <a href=\"https://www.gov.uk/government/publications/form-d11-application-notice\" target=\"_blank\" rel=\"noopener noreferrer\">D11 form</a> and send it to the court. Details of where to send it are on the form.";
    }

    public static String getFirstInTimeLabel() {
        return
            """
            <details>
            <summary>
            <span>
            Making a sole application for a conditional order
            </span>
            </summary>
            <div>
            <p>
            If you want to progress the application but you do not think the other party will confirm the joint application
            for a conditional order, then you can change to a sole application.
            This means the other party will become the respondent.
            </p>
            <p>
            You should still continue to draft and submit a joint conditional order application.
            If the other party does not confirm the application then you will be able to change to a sole application online.
            </p>
            <p>
            If you want to apply for a conditional order as a sole applicant now, then you need to <a href="https://www.gov.uk/government/publications/form-d84-application-for-a-decree-nisi-conditional-order-or-judicial-separation-decreeorder" target="_blank" rel="noopener noreferrer">download and fill out a paper D84 form</a>. Details of where to post it are on the form. Your application will be lodged as soon as the form is received by the court but it could take up to 3 weeks to process the application.
            </p>
            </div>
            </details>
            <br>
            """;
    }

    public static String getSecondInTimeLabel() {
        return
            """
            <details>
            <summary>
            <span>
            If you want to continue as a sole applicant
            </span>
            </summary>
            <div>
            <p>
            The other applicant has already confirmed they want to continue with the joint application.
            The quickest way for you to progress the ${labelContentDivorceOrEndingCivilPartnership} is for you to also confirm
            you want to continue with the joint application.
            </p>
            <p>
            If you want to change to a sole application then it will delay the ${labelContentDivorceOrEndingCivilPartnership}.
            You will have to make a separate application by <a href="https://www.gov.uk/government/publications/form-d84-application-for-a-decree-nisi-conditional-order-or-judicial-separation-decreeorder" target="_blank" rel="noopener noreferrer">downloading and filling out a D84 paper form.</a> Details of where to send it are on the form. If you want to change to a sole application then you should go back to ‘manage cases’, sign out and send in the form.
            </p>
            <p>
            The quickest way to progress the ${labelContentDivorceOrEndingCivilPartnership}
            is to confirm you want to continue with the joint application.
            </p>
            </div>
            </details>
            <br>
            """;
    }
}
