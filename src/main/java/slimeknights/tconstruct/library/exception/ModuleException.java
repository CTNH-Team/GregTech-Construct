package slimeknights.tconstruct.library.exception;

public class ModuleException extends OOException {
    public ModuleException(String message) {
        super("ModuleException | " + message);
    }
}
