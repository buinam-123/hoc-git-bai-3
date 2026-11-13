package calc.common;

/** Nhịp tim mỗi service gửi định kỳ để gateway biết service còn sống. */
public record Heartbeat(String op, String instance, long ts) {}
