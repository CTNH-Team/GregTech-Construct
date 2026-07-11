package slimeknights.tconstruct.library.exception;

import slimeknights.tconstruct.TConstruct;

public class OOException extends RuntimeException {
    public OOException(String message) {
        super("[" + TConstruct.MOD_ID + "]: " + message);
    }
}
