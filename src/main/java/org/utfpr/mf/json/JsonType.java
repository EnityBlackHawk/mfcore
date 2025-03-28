package org.utfpr.mf.json;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JsonType {

    @SerializedName("object")
    OBJECT("object"),
    @SerializedName("array")
    ARRAY("array"),
    @SerializedName("string")
    STRING("string"),
    @SerializedName("number")
    NUMBER("number"),
    @SerializedName("integer")
    INTEGER("integer");

    private final String value;

}
