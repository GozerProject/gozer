package org.gozer;

import com.google.common.base.Charsets;
import restx.SignatureKey;
import restx.factory.Module;
import restx.factory.Provides;

@Module
public class GozerModule {
    @Provides
    public SignatureKey signatureKey() {
        return new SignatureKey("5341679063568601943 gozer e372a614-d7a5-477b-9e3f-40c182b9f34d".getBytes(Charsets.UTF_8));
    }
}
