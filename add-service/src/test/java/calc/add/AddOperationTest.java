package calc.add;

import static org.junit.jupiter.api.Assertions.*;

import calc.common.CalcException;
import org.junit.jupiter.api.Test;

class AddOperationTest {

    private final AddOperation op = new AddOperation();

    @Test
    void addsExactly() throws Exception {
        assertEquals("5", op.compute("2", "3"));
        assertEquals("0.3", op.compute("0.1", "0.2"));
        assertEquals("-2.5", op.compute("1.5", "-4"));
        assertEquals("-2.5", op.compute("1,5", "-4"));
    }

    @Test
    void rejectsBadInput() {
        assertTrue(assertThrows(CalcException.class, () -> op.compute("abc", "1")).getMessage().contains("không phải là số"));
        assertTrue(assertThrows(CalcException.class, () -> op.compute("1", null)).getMessage().contains("Thiếu toán hạng"));
        assertThrows(CalcException.class, () -> op.compute("1e999999999", "1"));
    }
}
