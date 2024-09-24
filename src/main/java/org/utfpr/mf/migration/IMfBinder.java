package org.utfpr.mf.migration;

public interface IMfBinder {

   IMfBinder bind(String key, Object value);
   void inject(Object target);

}
