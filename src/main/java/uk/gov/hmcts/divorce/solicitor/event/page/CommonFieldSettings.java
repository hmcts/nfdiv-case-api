package uk.gov.hmcts.divorce.solicitor.event.page;

public final class CommonFieldSettings {

    public static final String SOLICITOR_NFD_PREVIEW_BANNER =
        "![NFD Preview](https://raw.githubusercontent.com/hmcts/nfdiv-case-api/master/resources/image/SolicitorNfdPreviewBanner.jpg "
            + "\"You are using the NFD preview\")";

    public static final String SOLICITOR_NFD_JOINT_PREVIEW_BANNER =
        "![NFD Preview Joint](https://raw.githubusercontent.com/hmcts/nfdiv-case-api/master/resources/image/SolicitorNfdPreviewJointApplicationBanner.jpg "
            + "\"You are creating a Joint applicant in the NFD preview\")";

    public static final String DARK_HORIZONTAL_RULE =
        "![Dark Rule](https://raw.githubusercontent.com/hmcts/nfdiv-case-api/master/resources/image/LabelDarkHorizontalRule.png)";

    public static final String SOLE_APPLICATION_CONDITION = "applicationType=\"soleApplication\"";
    public static final String JOINT_APPLICATION_CONDITION = "applicationType=\"jointApplication\"";

    private CommonFieldSettings() {
    }
}
