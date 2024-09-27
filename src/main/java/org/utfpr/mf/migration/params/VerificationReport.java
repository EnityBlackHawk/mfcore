package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationReport {

    private boolean isCountTestSucceeded;
    private boolean isHashTestSucceeded;

    private String countTestMessage;
    private String hashTestMessage;

}
