package calc.common;

/** Một phép tính. Mỗi microservice cung cấp đúng một bean Operation. */
public interface Operation {

    /** Tên ngắn dùng làm topic: add, subtract, ... */
    String name();

    /**
     * @param a toán hạng thứ nhất (dạng chuỗi để giữ nguyên độ chính xác)
     * @param b toán hạng thứ hai, null với phép tính một ngôi
     * @return kết quả dạng chuỗi
     */
    String compute(String a, String b) throws CalcException;
}
