package uk.gov.hmcts.divorce.testutil;

import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Search;
import uk.gov.hmcts.ccd.sdk.api.Search.SearchBuilder;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.plexus.util.ReflectionUtils.getValueIncludingSuperclasses;
import static org.junit.platform.commons.util.ReflectionUtils.findMethod;

public final class ConfigTestUtil {

    private ConfigTestUtil() {
    }

    public static ConfigBuilderImpl<CaseData, State, UserRole> createCaseDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            CaseData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<ExceptionRecord, ExceptionRecordState, UserRole> createExceptionRecordConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            ExceptionRecord.class,
            ExceptionRecordState.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(ExceptionRecordState.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> createBulkActionConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            BulkActionCaseData.class,
            BulkActionState.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(BulkActionState.class.getEnumConstants())));
    }

    @SuppressWarnings({"unchecked"})
    public static <T, S, R extends HasRole> Map<String, Event<T, R, S>> getEventsFrom(
        final ConfigBuilder<T, S, R> configBuilder) {

        return (Map<String, Event<T, R, S>>) findMethod(ConfigBuilderImpl.class, "getEvents")
            .map(method -> {
                try {
                    method.setAccessible(true);
                    return method.invoke(configBuilder);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new AssertionError("Unable to invoke ConfigBuilderImpl.class method getEvents", e);
                }
            })
            .orElseThrow(() -> new AssertionError("Unable to find ConfigBuilderImpl.class method getEvents"));
    }

    public static <T, S, R extends HasRole> Search<T, R> getSearchInputFields(
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        return getSearchFor("searchInputFields", configBuilder);
    }

    public static <T, S, R extends HasRole> Search<T, R> getSearchResultFields(
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        return getSearchFor("searchResultFields", configBuilder);
    }

    public static <T, S, R extends HasRole> Search<T, R> getWorkBasketInputFields(
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        return getWorkBasketFor("workBasketInputFields", configBuilder);
    }

    public static <T, S, R extends HasRole> Search<T, R> getWorkBasketResultFields(
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        return getWorkBasketFor("workBasketResultFields", configBuilder);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T, S, R extends HasRole> Search<T, R> getSearchFor(
        final String fieldName,
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        final List<SearchBuilder> searchInputFields =
            (List<SearchBuilder>) getValueIncludingSuperclasses(fieldName, configBuilder);
        final var searchInputBuilder = searchInputFields.get(0);
        return searchInputBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T, S, R extends HasRole> Search<T, R> getWorkBasketFor(
        final String fieldName,
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {

        final List<SearchBuilder> workBasketInputFields =
            (List<SearchBuilder>) getValueIncludingSuperclasses(fieldName, configBuilder);
        final var workBasketBuilder = workBasketInputFields.get(0);
        return workBasketBuilder.build();
    }
}
