package calc.common;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Tiện ích xử lý số bằng BigDecimal (chính xác, không dính sai số dấu phẩy động). */
public final class Num {

    /** Độ chính xác 15 chữ số có nghĩa cho phép chia, căn, lũy thừa số thực. */
    public static final MathContext MC = new MathContext(15, RoundingMode.HALF_UP);

    private static final int MAX_INPUT_LENGTH = 1000;
    private static final int MAX_DIGITS = 1000;

    private Num() {}

    public static BigDecimal parse(String text, String label) throws CalcException {
        if (text == null || text.isBlank()) {
            throw new CalcException("Thiếu toán hạng \"" + label + "\"");
        }
        String t = text.trim().replace(',', '.');
        if (t.length() > MAX_INPUT_LENGTH) {
            throw new CalcException("Toán hạng \"" + label + "\" quá dài");
        }
        BigDecimal v;
        try {
            v = new BigDecimal(t);
        } catch (NumberFormatException e) {
            throw new CalcException("Toán hạng \"" + label + "\" không phải là số hợp lệ");
        }
        // Chặn các số như 1e999999999 làm tràn bộ nhớ khi in ra
        if (v.precision() > MAX_DIGITS || Math.abs((long) v.scale()) > MAX_DIGITS) {
            throw new CalcException("Toán hạng \"" + label + "\" quá lớn");
        }
        return v;
    }

    public static boolean isInteger(BigDecimal v) {
        return v.signum() == 0 || v.stripTrailingZeros().scale() <= 0;
    }

    /** Dạng chuỗi không dùng ký hiệu khoa học, bỏ số 0 thừa: 2.50 → 2.5, 1E+3 → 1000. */
    public static String plain(BigDecimal v) {
        if (v.signum() == 0) {
            return "0";
        }
        return v.stripTrailingZeros().toPlainString();
    }
}
