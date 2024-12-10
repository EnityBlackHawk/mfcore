package org.utfpr.mf.migration;

import org.utfpr.mf.annotation.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.interfaces.IMfBinder;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
        var fields = new ArrayList<Field>(Arrays.stream(target.getClass().getDeclaredFields()).toList());

        var sup = target.getClass().getSuperclass();
        while (sup != null) {
            fields.addAll(Arrays.stream(sup.getDeclaredFields()).toList());
            sup = sup.getSuperclass();
        }

        fields.stream().filter(f -> f.isAnnotationPresent(Injected.class)).forEach(f -> {
            var an = f.getAnnotation(Injected.class);
            f.setAccessible(true);
            try {
                f.set(target, bindings.get(an.value() == DefaultInjectParams.UNSET ? f.getName() : an.value().getValue()));

                if(f.get(target) == null && an.required()) {
                    throw new RuntimeException("Injected field " + f.getName() + " ( " + target.getClass().getName() + " ) is required, but none was provided or exported");
                }

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
