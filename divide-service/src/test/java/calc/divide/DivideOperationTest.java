package calc.divide;

import static org.junit.jupiter.api.Assertions.*;

import calc.common.CalcException;
import org.junit.jupiter.api.Test;

class DivideOperationTest {

    private final DivideOperation op = new DivideOperation();

    @Test
    void divides() throws Exception {
        assertEquals("2.5", op.compute("10", "4"));
        assertEquals("42", op.compute("84", "2"));
        assertEquals("0.333333333333333", op.compute("1", "3"));
    }

    @Test
    void rejectsDivisionByZero() {
        assertTrue(assertThrows(CalcException.class, () -> op.compute("1", "0")).getMessage().contains("chia cho 0"));
    }
}
