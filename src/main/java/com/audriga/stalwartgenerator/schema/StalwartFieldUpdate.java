package com.audriga.stalwartgenerator.schema;

import com.audriga.jmap.gson.Mutability;

public enum StalwartFieldUpdate {
    mutable(Mutability.MUTABLE),
    immutable(Mutability.IMMUTABLE),
    serverSet(Mutability.SERVER_SET);

    private final Mutability mutability;

    StalwartFieldUpdate(Mutability mutability) {
        this.mutability = mutability;
    }

    public Mutability mutability() {
        return mutability;
    }
}
