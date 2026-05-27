package io.github.konradkelly.centroidfinder;

public record JobProcessResult(int exitCode, String diagnostics, boolean timedOut) {
    public static JobProcessResult completed(int exitCode, String diagnostics) {
        return new JobProcessResult(exitCode, diagnostics, false);
    }

    public static JobProcessResult timedOut(String diagnostics) {
        return new JobProcessResult(-1, diagnostics, true);
    }
}
