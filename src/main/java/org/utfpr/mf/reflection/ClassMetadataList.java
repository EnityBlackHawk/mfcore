package org.utfpr.mf.reflection;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ClassMetadataList extends ArrayList<ClassMetadata> {

    public ClassMetadataList(int initialCapacity) {
        super(initialCapacity);
    }

    public ClassMetadataList() {
    }

    public ClassMetadataList(@NotNull Collection<? extends ClassMetadata> c) {
        super(c);
    }
}
