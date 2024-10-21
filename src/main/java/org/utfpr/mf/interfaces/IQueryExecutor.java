package org.utfpr.mf.interfaces;

import java.util.Iterator;

public interface IQueryExecutor<TInput, TOutput> extends Iterator<IQuery<TInput, TOutput>> {

    int getTimesEach();
    TOutput executeActual();
    TOutput executeAndNext();
    long executeAndGetTime();
}
