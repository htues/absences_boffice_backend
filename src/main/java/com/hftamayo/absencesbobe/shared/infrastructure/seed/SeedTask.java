package com.hftamayo.absencesbobe.shared.infrastructure.seed;

public interface SeedTask {
    /**
     * Identifier matched against entries in seed.include.
     * For your current style, use filenames like "companies.yaml".
     */
    String id();

    /** Lower runs earlier. */
    default int order() {
        return 0;
    }

    /** Must be safe to run multiple times (idempotent). */
    void run();
}