package calc.factorial;

import static org.junit.jupiter.api.Assertions.*;

import calc.common.CalcException;
import org.junit.jupiter.api.Test;

class FactorialOperationTest {

    private final FactorialOperation op = new FactorialOperation();

    @Test
    void factorials() throws Exception {
        assertEquals("1", op.compute("0", null));
        assertEquals("120", op.compute("5", null));
        assertEquals("120", op.compute("5.0", null));
        assertEquals("15511210043330985984000000", op.compute("25", null));
    }

    @Test
    void rejectsInvalid() {
        assertTrue(assertThrows(CalcException.class, () -> op.compute("-1", null)).getMessage().contains("số âm"));
        assertTrue(assertThrows(CalcException.class, () -> op.compute("2.5", null)).getMessage().contains("số nguyên"));
        assertTrue(assertThrows(CalcException.class, () -> op.compute("99999", null)).getMessage().contains("quá lớn"));
    }
}
