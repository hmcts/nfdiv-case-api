package uk.gov.hmcts.divorce.sow014.lib;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;

@Service
public class CallbackEnumerator {
    private final List<ResolvedCCDConfig<?, ?, ?>> configs;
    private Set<String> submittedCallbacks;
    private Set<String> aboutToSubmitCallbacks;

    @Autowired
    public CallbackEnumerator(List<ResolvedCCDConfig<?, ?, ?>> configs) {
        this.configs = configs;
        var cfg = configs.stream().filter(x -> x.getCaseType().equals("NFD")).findFirst();
        submittedCallbacks = new HashSet<>();
        aboutToSubmitCallbacks = new HashSet<>();
        cfg.get().getEvents().forEach((x, y) -> {
            if (y.getAboutToSubmitCallback() != null) {
                aboutToSubmitCallbacks.add(x);
            }
            if (y.getSubmittedCallback() != null) {
                submittedCallbacks.add(x);
            }
        });
    }

    public boolean hasAboutToSubmitCallbackForEvent(String event) {
        return aboutToSubmitCallbacks.contains(event);
    }
    public boolean hasSubmittedCallbackForEvent(String event) {
        return submittedCallbacks.contains(event);
    }

}
