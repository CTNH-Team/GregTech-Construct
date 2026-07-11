package slimeknights.tconstruct.library.exception;

public class DamageException extends OOException {
    public DamageException(String message) {
        super("DamageException | " + message);
    }
}
