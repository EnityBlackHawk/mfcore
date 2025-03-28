package org.utfpr.mf.interfaces;

public interface IQuery<TInput, TOutput> {

    String getName();
    TOutput execute(TInput input);
    String getStringQuery();
    default long cronExecute(TInput input) {
        throw new UnsupportedOperationException();
    }

}
