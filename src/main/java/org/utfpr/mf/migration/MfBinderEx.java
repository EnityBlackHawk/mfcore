package org.utfpr.mf.migration;

import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.interfaces.IMfBinder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class MfBinderEx implements IMfBinder {

    protected final Map<String, Object> bindings = new HashMap<>();

    @Override
    public MfBinderEx bind(String key, Object value) {
        bindings.put(key, value);
        return this;
    }

    @Override
    public void inject(Object target) {
        Arrays.stream(target.getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(Injected.class)).forEach(f -> {
            var an = f.getAnnotation(Injected.class);
            f.setAccessible(true);
            try {
                f.set(target, bindings.get(an.value() == DefaultInjectParams.UNSET ? f.getName() : an.value().getValue()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Object get(String key) {
        return bindings.get(key);
    }

    @Override
    public boolean has(String key) {
        return bindings.containsKey(key);
    }
}
