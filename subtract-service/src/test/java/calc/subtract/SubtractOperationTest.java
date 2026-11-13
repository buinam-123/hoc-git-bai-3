package calc.subtract;

import static org.junit.jupiter.api.Assertions.*;

import calc.common.CalcException;
import org.junit.jupiter.api.Test;

class SubtractOperationTest {

    private final SubtractOperation op = new SubtractOperation();

    @Test
    void subtracts() throws Exception {
        assertEquals("5.5", op.compute("10", "4.5"));
        assertEquals("-1", op.compute("2", "3"));
    }
}
