package calc.sqrt;

import static org.junit.jupiter.api.Assertions.*;

import calc.common.CalcException;
import org.junit.jupiter.api.Test;

class SqrtOperationTest {

    private final SqrtOperation op = new SqrtOperation();

    @Test
    void roots() throws Exception {
        assertEquals("12", op.compute("144", null));
        assertEquals("1.4142135623731", op.compute("2", null));
        assertEquals("0", op.compute("0", null));
    }

    @Test
    void rejectsNegative() {
        assertTrue(assertThrows(CalcException.class, () -> op.compute("-1", null)).getMessage().contains("số âm"));
    }
}
