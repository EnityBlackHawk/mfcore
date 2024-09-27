package org.utfpr.mf.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum DefaultInjectParams {
    UNSET("unset"),
    LLM_KEY("llm_key"),
    DB_METADATA("dbMetadata"),
    MONGO_CONNECTION("mongoConnection");


    private final String value;

    DefaultInjectParams(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
