package calc.multiply;

import calc.common.CalcException;
import calc.common.Num;
import calc.common.Operation;
import org.springframework.stereotype.Component;

@Component
public class MultiplyOperation implements Operation {
    @Override
    public String name() {
        return "multiply";
    }

    @Override
    public String compute(String a, String b) throws CalcException {
        return Num.plain(Num.parse(a, "a").multiply(Num.parse(b, "b")));
    }
}
