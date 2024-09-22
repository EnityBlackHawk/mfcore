package org.utfpr.mf.migration;

public class MfMigrationStepFactory {

    public IMfMigrationStep createAcquireMetadataStep() {
        return new AcquireMetadataStep();
    }

}
