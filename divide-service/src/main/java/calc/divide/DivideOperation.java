package calc.divide;

import calc.common.CalcException;
import calc.common.Num;
import calc.common.Operation;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DivideOperation implements Operation {
    @Override
    public String name() {
        return "divide";
    }

    @Override
    public String compute(String a, String b) throws CalcException {
        BigDecimal dividend = Num.parse(a, "số bị chia");
        BigDecimal divisor = Num.parse(b, "số chia");
        if (divisor.signum() == 0) {
            throw new CalcException("Không thể chia cho 0");
        }
        return Num.plain(dividend.divide(divisor, Num.MC));
    }
}
