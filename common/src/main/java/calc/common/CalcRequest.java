package calc.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Message gửi từ gateway tới service. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalcRequest(String id, String op, String a, String b) {}
