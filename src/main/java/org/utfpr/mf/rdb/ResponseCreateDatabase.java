package org.utfpr.mf.rdb;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResponseCreateDatabase {
    private String executeSQL;
    private TempDatabase tempDatabase;

}
