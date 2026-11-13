package calc.factorial;

import calc.common.CalcException;
import calc.common.Num;
import calc.common.Operation;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.springframework.stereotype.Component;

@Component
public class FactorialOperation implements Operation {

    private static final int MAX_N = 3000;

    @Override
    public String name() {
        return "factorial";
    }

    @Override
    public String compute(String a, String b) throws CalcException {
        BigDecimal n = Num.parse(a, "n");
        if (!Num.isInteger(n)) {
            throw new CalcException("Giai thừa chỉ áp dụng cho số nguyên");
        }
        if (n.signum() < 0) {
            throw new CalcException("Giai thừa không xác định với số âm");
        }
        if (n.compareTo(BigDecimal.valueOf(MAX_N)) > 0) {
            throw new CalcException("n quá lớn (tối đa " + MAX_N + ")");
        }
        int k = n.intValueExact();
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= k; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result.toString();
    }
}
