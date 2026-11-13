package calc.power;

import static org.junit.jupiter.api.Assertions.*;

import calc.common.CalcException;
import org.junit.jupiter.api.Test;

class PowerOperationTest {

    private final PowerOperation op = new PowerOperation();

    @Test
    void powers() throws Exception {
        assertEquals("1024", op.compute("2", "10"));
        assertEquals("0.25", op.compute("2", "-2"));
        assertEquals("1", op.compute("0", "0"));
        assertEquals("3", op.compute("9", "0.5"));
        assertEquals(1001, op.compute("10", "1000").length());
        assertEquals("1267650600228229401496703205376", op.compute("2", "100"));
    }

    @Test
    void rejectsInvalid() {
        assertTrue(assertThrows(CalcException.class, () -> op.compute("0", "-1")).getMessage().contains("số mũ âm"));
        assertTrue(assertThrows(CalcException.class, () -> op.compute("-8", "0.5")).getMessage().contains("không nguyên"));
        assertTrue(assertThrows(CalcException.class, () -> op.compute("10", "1000.5")).getMessage().contains("giới hạn"));
    }
}
