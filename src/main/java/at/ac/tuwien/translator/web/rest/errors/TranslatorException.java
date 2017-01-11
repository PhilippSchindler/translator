package at.ac.tuwien.translator.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class TranslatorException extends RuntimeException {
    public TranslatorException() {
        super();
    }
    public TranslatorException(String message, Throwable cause) {
        super(message, cause);
    }
    public TranslatorException(String message) {
        super(message);
    }
    public TranslatorException(Throwable cause) {
        super(cause);
    }
}
