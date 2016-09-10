package org.spongepowered.special.instance;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

/**
 * Built-in {@link InstanceType}s shipped by default
 */
public final class InstanceTypes {

    // SORTFIELDS:ON

    /**
     * The default instance type. Up to 6 players battle until one man is left standing.
     */
    public static final InstanceType LAST_MAN_STANDING = DummyObjectProvider.createFor(InstanceType.class, "last_man_standing");

    // SORTFIELDS:OFF
}
