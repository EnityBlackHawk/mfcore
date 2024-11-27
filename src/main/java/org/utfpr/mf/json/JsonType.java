package org.utfpr.mf.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JsonType {

    OBJECT("object"),
    ARRAY("array"),
    STRING("string");

    private final String value;

}
