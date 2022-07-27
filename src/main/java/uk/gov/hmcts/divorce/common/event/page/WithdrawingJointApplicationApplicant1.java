package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class WithdrawingJointApplicationApplicant1 implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("WithdrawingJointApplicationApplicant1")
            .pageLabel("Withdrawing a joint application")
            .showCondition("coApplicant1ApplyForConditionalOrder=\"No\"")
            .label("withdrawLabelApplicant1", "If you want to withdraw this joint application then both parties need to jointly fill out a D11 form and send it to the court. Details of where to send it are on the form.")
            .label("firstInTimeApplicant1",
                "<details>" +
                    "  <summary>" +
                    "    <span>" +
                    "      Making a sole application for a conditional order" +
                    "    </span>" +
                    "  </summary>" +
                    "  <div>" +
                    "    <p>If you want to progress the application but you do not think the other party will confirm the joint application for a conditional order, then you can change to a sole application. This means the other party will become the respondent.</p>" +
                    "    <p>You should still continue to draft and submit a joint conditional order application. If the other party does not confirm the application then you will be able to change to a sole application online.</p>" +
                    "    <p>If you want to apply for a conditional order as a sole applicant now, then you need to download and fill out a paper D84 form. Details of where to post it are on the form. Your application will be lodged as soon as the form is received by the court but it could take up to 3 weeks to process the application.</p>" +
                    "  </div>" +
                    "</details>" +
                    "<br>")
            .label("secondInTimeApplicant1",
                "<details>" +
                    "  <summary>" +
                    "    <span>" +
                    "      If you want to continue as a sole applicant" +
                    "    </span>" +
                    "  </summary>" +
                    "  <div>" +
                    "    <p>The other applicant has already confirmed they want to continue with the joint application. The quickest way for you to progress the ${labelContentDivorceOrEndingCivilPartnership} is for you to also confirm you want to continue with the joint application.</p>" +
                    "    <p>If you want to change to a sole application then it will delay the ${labelContentDivorceOrEndingCivilPartnership}. You will have to make a separate application by downloading and filling out a D84 paper form. Details of where to send it are on the form. If you want to change to a sole application then you should go back to ‘manage cases’, sign out and send in the form.</p>" +
                    "    <p>The quickest way to progress the ${labelContentDivorceOrEndingCivilPartnership} is to confirm you want to continue with the joint application.</p>" +
                    "  </div>" +
                    "</details>" +
                    "<br>")
            .done();
    }
}
