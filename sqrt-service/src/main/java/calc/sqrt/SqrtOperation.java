package calc.sqrt;

import calc.common.CalcException;
import calc.common.Num;
import calc.common.Operation;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class SqrtOperation implements Operation {
    @Override
    public String name() {
        return "sqrt";
    }

    @Override
    public String compute(String a, String b) throws CalcException {
        BigDecimal x = Num.parse(a, "a");
        if (x.signum() < 0) {
            throw new CalcException("Không thể lấy căn bậc hai của số âm");
        }
        if (x.signum() == 0) {
            return "0";
        }
        return Num.plain(x.sqrt(Num.MC));
    }
}
