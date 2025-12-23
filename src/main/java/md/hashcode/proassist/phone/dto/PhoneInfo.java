package md.hashcode.proassist.phone.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PhoneInfo(
        @Size(max = 500) String description,

        @DecimalMin(value = "0.0")
        @Digits(integer = 12, fraction = 2)
        BigDecimal price,

        Currency currency,

        @Size(max = 500) String address,

        Services services,

        @Size(max = 5000) String comment,

        Boolean visited,

        @Min(1) @Max(5)
        Integer rating,

        Finished finished,

        @Size(max = 2000) String sourceUrl
) {
    public enum Currency {MDL, EUR, USD}

    public enum Finished {YES, NO, PARTIALLY, HAND, ORA}

    public record Services(
            Boolean owc,
            Boolean ana
    ) {
    }
}
