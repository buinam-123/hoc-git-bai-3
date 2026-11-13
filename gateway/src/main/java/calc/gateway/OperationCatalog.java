package calc.gateway;

import calc.common.Topics;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Danh sách các phép tính mà gateway biết. */
@Component
public class OperationCatalog {

    public record Def(String id, String label, String symbol, int arity) {
        public String topic() {
            return Topics.requests(id);
        }
    }

    private final Map<String, Def> defs = new LinkedHashMap<>();

    public OperationCatalog() {
        add("add", "Cộng", "+", 2);
        add("subtract", "Trừ", "−", 2);
        add("multiply", "Nhân", "×", 2);
        add("divide", "Chia", "÷", 2);
        add("sqrt", "Căn bậc 2", "√", 1);
        add("power", "Lũy thừa", "xʸ", 2);
        add("factorial", "Giai thừa", "n!", 1);
    }

    private void add(String id, String label, String symbol, int arity) {
        defs.put(id, new Def(id, label, symbol, arity));
    }

    public Def find(String id) {
        return id == null ? null : defs.get(id);
    }

    public Collection<Def> all() {
        return defs.values();
    }
}
