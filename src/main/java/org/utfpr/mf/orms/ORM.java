package org.utfpr.mf.orms;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.utfpr.mf.prompt.Framework;

@Data
@AllArgsConstructor
public abstract class ORM {

    protected Framework name;
    protected String idAnnotation;
    protected String referenceAnnotation;
    protected String documentAnnotation;

}
