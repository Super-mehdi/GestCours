package ma.emi.backend.exception;

public class SimulatedCrashException extends RuntimeException {
    public SimulatedCrashException(String appVersion) {
        super("Simulated crash in new checkout engine (" + appVersion + ")");
    }
}
