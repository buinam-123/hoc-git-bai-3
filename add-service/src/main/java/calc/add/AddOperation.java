package calc.add;

import calc.common.CalcException;
import calc.common.Num;
import calc.common.Operation;
import org.springframework.stereotype.Component;

@Component
public class AddOperation implements Operation {
    @Override
    public String name() {
        return "add";
    }

    @Override
    public String compute(String a, String b) throws CalcException {
        return Num.plain(Num.parse(a, "a").add(Num.parse(b, "b")));
    }
}
