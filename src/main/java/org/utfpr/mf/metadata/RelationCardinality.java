package org.utfpr.mf.metadata;

import com.google.gson.GsonBuilder;

import java.util.Objects;

public class RelationCardinality {
    private String source;
    private String target;
    private int min;
    private int max;
    private double avg;

    public RelationCardinality() {
    }

    public RelationCardinality(
            String source,
            String target,
            int min,
            int max,
            double avg

    ) {
        this.source = source;
        this.target = target;
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    public RelationCardinality(Relations relation, int min, int max, double avg) {
        this(relation.table_source, relation.table_target, min, max, avg);
    }

    @Override
    public String toString() {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getAvg() {
        return avg;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RelationCardinality) obj;
        return Objects.equals(this.source, that.source) &&
                Objects.equals(this.target, that.target) &&
                this.min == that.min &&
                this.max == that.max &&
                Double.doubleToLongBits(this.avg) == Double.doubleToLongBits(that.avg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, min, max, avg);
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }
}