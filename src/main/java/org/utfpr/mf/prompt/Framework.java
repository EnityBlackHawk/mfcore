package org.mf.langchain.prompt;

public enum Framework {

    SPRING_DATA ("Spring Data MongoDB"),
    IMPETUS_KUNDERA("Impetus Kundera"),
    DATANUCLEUS("DataNucleus");

    private final String fw;

    Framework(String fw) {
        this.fw = fw;
    }

    public String getFramework() {
        return fw;
    }

}
