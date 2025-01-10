package org.utfpr.mf.scorus;


import org.utfpr.mf.annotation.Export;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.migration.MfMigrationStepEx;
import org.utfpr.mf.migration.params.Model;

import java.io.PrintStream;
import java.util.HashMap;

public class ScorusStep extends MfMigrationStepEx<Model, Model> {

    @Export(DefaultInjectParams.METRICS_RESULT)
    private MetricsResult metricsResult;

    public ScorusStep() {
        this(System.out);
    }

    public ScorusStep(PrintStream printStream) {
        super("ScorusStep", printStream, Model.class, Model.class);
    }

    @Override
    public Object execute(Object input) {
        return executeHelper(this::process, input);
    }

    private Model process(Model model) {

        JsonSchemaList schema = model.getModels();
        metricsResult = new MetricsResult();

        BEGIN("Calculating colExistence:");
        HashMap<String, Integer> colExistence = new HashMap<>();
        for(var collection : schema) {
            BEGIN_SUB("Calculating colExistence for " + collection.getTitle());
            colExistence.put(collection.getTitle(), Metrics.colExistence(schema, collection.getTitle()));
        }
        metricsResult.setColExistence(colExistence);
        END();

        BEGIN("Calculating colDepth:");
        HashMap<String, Integer> colDepth = new HashMap<>();
        for(var collection : schema) {
            BEGIN_SUB("Calculating colDepth for " + collection.getTitle());
            colDepth.put(collection.getTitle(), Metrics.colDepth(collection));
        }
        metricsResult.setColDepth(colDepth);
        END();

        BEGIN("Calculating globalDepth:");
        metricsResult.setGlobalDepth(Metrics.globalDepth(schema));
        END();

        BEGIN("Calculating docWidth:");
        HashMap<String, Integer> docWidth = new HashMap<>();
        for(var collection : schema) {
            BEGIN_SUB("Calculating docWidth for " + collection.getTitle());
            docWidth.put(collection.getTitle(), Metrics.docWidth(collection));
        }
        metricsResult.setDocWidth(docWidth);
        END();

        BEGIN("Calculating refLoad:");
        HashMap<String, Integer> refLoad = new HashMap<>();
        for(var collection : schema) {
            BEGIN_SUB("Calculating refLoad for " + collection.getTitle());
            refLoad.put(collection.getTitle(), Metrics.refLoad(schema, collection.getTitle()));
        }
        metricsResult.setRefLoad(refLoad);
        END();

        return model;
    }
}
