package org.utfpr.mf.migration;

import org.utfpr.mf.interfaces.IQuery;
import org.utfpr.mf.interfaces.IQueryExecutor;
import org.utfpr.mf.migration.params.BenchmarkResult;
import org.utfpr.mf.migration.params.BenchmarkResultList;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class BenchmarkStep extends MfMigrationStepEx<IQueryExecutor, BenchmarkResultList> {


    public BenchmarkStep() {
        this(System.out);
    }

    public BenchmarkStep(PrintStream printStream) {
        super("BenchmarkStep", printStream, IQueryExecutor.class, BenchmarkResultList.class);
    }

    private BenchmarkResultList process(IQueryExecutor<?, ?> input) {
        BenchmarkResultList resultList = new BenchmarkResultList();
        BEGIN("Executing Queries");
        while(input.hasNext()) {
            ArrayList<Long> times = new ArrayList<>();
            for(int i = 0; i < input.getTimesEach(); i++) {
                long time = input.executeAndGetTime();
                if(time == -1) {
                    if(notifyError("Execution " + (i + 1) + ": result was invalid")) {
                        callStopSign();
                        return null;
                    }
                    time = 0;
                }
                BEGIN_SUB("Execution " + (i + 1) + " with " + time / Math.pow(10, 6));
                times.add(time);
            }
            IQuery query = input.next();
            times.remove(Collections.max(times));
            times.remove(Collections.min(times));
            long timeNano = times.stream().reduce(0L, Long::sum) / times.size();
            var br = new BenchmarkResult(query.getName(), query.getStringQuery(), timeNano);
            resultList.add(br);
            BEGIN("Query: " + query.getName() + " executed " + times.size() + " times, in " + br.getMilliseconds());
        }
        return resultList;
    }

    @Override
    public Object execute(Object input) {
        return executeHelper(this::process, input);
    }
}
