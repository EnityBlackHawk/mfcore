package org.utfpr.mf.prompt.desc;

import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.annotation.Optional;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.prompt.MigrationPreferences;
import org.utfpr.mf.prompt.Query;

import java.util.List;

public class PromptData4Desc {

    public String sqlTables;
    public MigrationPreferences migrationPreference;
    public Boolean allowReferences;
    public Framework framework;
    public @Nullable String cardinalityTable;
    public Boolean useMarkdown;
    public List<Query> queryList;
    public List<String> customPrompts;
    public @Optional Boolean referenceOnly = false;

}
