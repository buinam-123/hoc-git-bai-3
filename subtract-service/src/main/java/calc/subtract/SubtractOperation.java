package calc.subtract;

import calc.common.CalcException;
import calc.common.Num;
import calc.common.Operation;
import org.springframework.stereotype.Component;

@Component
public class SubtractOperation implements Operation {
    @Override
    public String name() {
        return "subtract";
    }

    @Override
    public String compute(String a, String b) throws CalcException {
        return Num.plain(Num.parse(a, "a").subtract(Num.parse(b, "b")));
    }
}
