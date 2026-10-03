package io.github.pigerzhu.onelab.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ShellProcessRunnerTest {
    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    @Test
    public void capturesSuccessfulOutput() {
        Shell.ProcessResult result = Shell.runProcess(
                command("echo onelab"), TimeUnit.SECONDS.toMillis(2));

        assertTrue(result.completedSuccessfully());
        assertEquals("onelab", result.output().trim());
    }

    @Test
    public void drainsOutputLargerThanProcessPipe() {
        String cmd = isWindows()
                ? "for /L %i in (1,1,20000) do @echo 01234567890123456789"
                : "yes 01234567890123456789 | head -n 20000";
        Shell.ProcessResult result = Shell.runProcess(
                command(cmd),
                TimeUnit.SECONDS.toMillis(10));

        assertTrue(result.completedSuccessfully());
        assertTrue(result.output().length() > 200_000);
    }

    @Test
    public void timesOutWithoutWaitingForProcessExit() {
        long started = System.nanoTime();
        String cmd = isWindows() ? "ping -n 6 127.0.0.1 >nul" : "sleep 5";
        Shell.ProcessResult result = Shell.runProcess(
                command(cmd), 100);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        assertFalse(result.completedSuccessfully());
        assertTrue(result.timedOut());
        assertTrue("elapsed=" + elapsedMs, elapsedMs < 2_000);
    }

    private static java.util.List<String> command(String command) {
        if (isWindows()) {
            return Arrays.asList("cmd.exe", "/d", "/c", command);
        }
        return Arrays.asList("sh", "-c", command);
    }
}
