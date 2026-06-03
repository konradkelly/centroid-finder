package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;

public class DefaultJobProcessLauncherTest {
    @Test
    public void drainStreamTruncatesToMaxDiagnostics() {
        DefaultJobProcessLauncher launcher = new DefaultJobProcessLauncher();
        // create input longer than 8192
        int len = 10000;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) bytes[i] = 'A';

        StringBuilder sink = new StringBuilder();
        launcher.drainStream(new ByteArrayInputStream(bytes), sink);
        // max is defined as 8*1024
        assertEquals(8 * 1024, sink.length());
    }
}
