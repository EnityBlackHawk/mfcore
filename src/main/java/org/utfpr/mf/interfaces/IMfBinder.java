package org.utfpr.mf.interfaces;

public interface IMfBinder {

   Object get(String key);
   boolean has(String key);
   IMfBinder bind(String key, Object value);
   void inject(Object target);

}
