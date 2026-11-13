package calc.power;

import calc.common.CalcException;
import calc.common.Num;
import calc.common.Operation;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.springframework.stereotype.Component;

@Component
public class PowerOperation implements Operation {

    /** Số mũ nguyên dương nhỏ hơn mức này và kết quả không quá lớn thì tính chính xác bằng BigInteger. */
    private static final BigDecimal MAX_EXACT_EXPONENT = BigDecimal.valueOf(1_000_000);
    private static final long MAX_EXACT_BITS = 200_000;

    @Override
    public String name() {
        return "power";
    }

    @Override
    public String compute(String a, String b) throws CalcException {
        BigDecimal base = Num.parse(a, "cơ số");
        BigDecimal exp = Num.parse(b, "số mũ");

        if (base.signum() == 0 && exp.signum() < 0) {
            throw new CalcException("0 không thể lũy thừa với số mũ âm");
        }
        boolean expIsInt = Num.isInteger(exp);
        if (base.signum() < 0 && !expIsInt) {
            throw new CalcException("Cơ số âm với số mũ không nguyên không có kết quả thực");
        }

        // Số nguyên ^ số mũ nguyên không âm: kết quả chính xác tuyệt đối
        if (Num.isInteger(base) && expIsInt && exp.signum() >= 0 && exp.compareTo(MAX_EXACT_EXPONENT) <= 0) {
            BigInteger bi = base.toBigInteger();
            int e = exp.intValueExact();
            long bits = (long) Math.max(1, bi.bitLength()) * e;
            if (bits <= MAX_EXACT_BITS) {
                return bi.pow(e).toString();
            }
        }

        double r = Math.pow(base.doubleValue(), exp.doubleValue());
        if (Double.isNaN(r) || Double.isInfinite(r)) {
            throw new CalcException("Kết quả vượt quá giới hạn biểu diễn");
        }
        return Num.plain(new BigDecimal(r).round(Num.MC));
    }
}
