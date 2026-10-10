package com.liminalreverie.story;

import java.util.Set;

/** Memory fragment bookkeeping. Ids 1..22 are story memories, 23..32 are optional. Text lives in lang files. */
public final class Memories {
    public static final int TOTAL = 32;
    public static final int MAIN = 22;
    /** Memories that are important to the main story (marked in the journal). */
    public static final Set<Integer> KEY = Set.of(1, 5, 9, 14, 18, 22, 32);

    private Memories() {}

    public static boolean optional(int id) {
        return id > MAIN;
    }
}
