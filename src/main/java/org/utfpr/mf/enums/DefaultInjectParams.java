package org.utfpr.mf.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum DefaultInjectParams {
    UNSET("unset"),
    LLM_KEY("llm_key"),
    DB_METADATA("dbMetadata"),
    PROMPT_DATA_VERSION("promptDataVersion"),
    MONGO_CREDENTIALS("mongoCredentials"),
    MIGRATION_SPEC("migrationSpec"),
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
