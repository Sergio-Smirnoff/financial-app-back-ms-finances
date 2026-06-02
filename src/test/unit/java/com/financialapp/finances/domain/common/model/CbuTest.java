package com.financialapp.finances.domain.common.model;

import com.financialapp.finances.domain.exception.InvalidCbuException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CbuTest {

    private static final String VALID = "0170099220000067797370"; // 22 digits

    @Test void acceptsTwentyTwoDigits() {
        assertThat(new Cbu(VALID).cbuNumber()).isEqualTo(VALID);
    }

    @Test void rejectsWrongLength() {
        assertThatThrownBy(() -> new Cbu("12345"))
            .isInstanceOf(InvalidCbuException.class);
    }

    @Test void rejectsNonDigits() {
        assertThatThrownBy(() -> new Cbu("01700992200000677973AB"))
            .isInstanceOf(InvalidCbuException.class);
    }

    @Test void rejectsNull() {
        assertThatThrownBy(() -> new Cbu(null))
            .isInstanceOf(InvalidCbuException.class);
    }

    @Test void exposesAnExternalInstallmentSentinelOfTwentyTwoZeros() {
        assertThat(Cbu.EXTERNAL_INSTALLMENT_CBU.cbuNumber()).isEqualTo("0000000000000000000000");
        assertThat(Cbu.EXTERNAL_INSTALLMENT_CBU).isEqualTo(new Cbu("0000000000000000000000"));
    }
}
