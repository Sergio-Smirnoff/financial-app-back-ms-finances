package com.financialapp.finances.domain.exception;

public class InvalidMoneyException extends DomainException {
    public InvalidMoneyException(String reason) {
        super(DomainErrorCode.INVALID_MONEY, "Invalid money amount: " + reason);
    }
}
