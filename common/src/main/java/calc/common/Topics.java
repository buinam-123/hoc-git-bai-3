package calc.common;

public final class Topics {
    public static final String RESULTS = "calc.results";
    public static final String HEARTBEAT = "calc.heartbeat";

    private Topics() {}

    public static String requests(String operation) {
        return "calc." + operation + ".requests";
    }
}
