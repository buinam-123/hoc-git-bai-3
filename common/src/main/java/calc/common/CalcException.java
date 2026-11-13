package calc.common;

/** Lỗi nghiệp vụ (chia cho 0, căn số âm...). Thông điệp được trả thẳng cho người dùng. */
public class CalcException extends Exception {
    public CalcException(String message) {
        super(message);
    }
}
