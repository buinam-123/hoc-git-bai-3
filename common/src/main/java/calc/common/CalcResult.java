package calc.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Message service trả về gateway qua topic calc.results. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalcResult(String id, String op, String service, String instance,
                         boolean ok, String result, String error, double processedMs) {}
