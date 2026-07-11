package slimeknights.tconstruct.library.exception;

public class ParameterException extends OOException {
    public ParameterException(String message) {
        super("ParameterException | " + message);
    }
}
