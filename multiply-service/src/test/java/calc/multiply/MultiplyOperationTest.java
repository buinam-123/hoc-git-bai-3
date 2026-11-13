package calc.multiply;

import static org.junit.jupiter.api.Assertions.*;

import calc.common.CalcException;
import org.junit.jupiter.api.Test;

class MultiplyOperationTest {

    private final MultiplyOperation op = new MultiplyOperation();

    @Test
    void multiplies() throws Exception {
        assertEquals("-42", op.compute("-6", "7"));
        assertEquals("0.06", op.compute("0.2", "0.3"));
        assertEquals("0", op.compute("0", "99"));
    }
}
