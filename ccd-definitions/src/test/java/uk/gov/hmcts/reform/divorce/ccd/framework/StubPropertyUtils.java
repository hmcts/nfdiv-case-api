package uk.gov.hmcts.reform.divorce.ccd.framework;

import de.cronn.reflection.util.TypedPropertyGetter;
import uk.gov.hmcts.ccd.sdk.types.PropertyUtils;

import java.lang.annotation.Annotation;

public class StubPropertyUtils implements PropertyUtils {

    @Override
    public <T, A extends Annotation> A getAnnotationOfProperty(final Class<T> entityType,
                                                               final TypedPropertyGetter<T, ?> propertyGetter,
                                                               final Class<A> annotationClass) {
        return null;
    }

    @Override
    public <U, T> Class<T> getPropertyType(final Class<U> c, final TypedPropertyGetter<U, T> getter) {
        return null;
    }

    @Override
    public <U> String getPropertyName(final Class<U> c, final TypedPropertyGetter<U, ?> getter) {
        return null;
    }
}
