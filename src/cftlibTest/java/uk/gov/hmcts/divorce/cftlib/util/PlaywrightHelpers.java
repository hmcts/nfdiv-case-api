package uk.gov.hmcts.divorce.cftlib.util;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

public class PlaywrightHelpers {
    public static final Page.WaitForURLOptions LONG_WAIT = new Page.WaitForURLOptions().setTimeout(30000);
    public static final int RETRIES = 10;

    private PlaywrightHelpers() {
    }

    public static SelectOption select(String label) {
        return new SelectOption().setLabel(label);
    }
}
