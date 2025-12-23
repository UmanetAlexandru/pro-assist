package md.hashcode.proassist.phone;

public final class PhoneKey {
    private PhoneKey() {
    }

    /**
     * Normalizes a phone input into a safe folder key.
     * Allowed output: leading '+' optional, then digits only.
     */
    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("Phone is required");

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Phone is required");

        // Keep '+' only if it's the first char, remove everything else that's not digit.
        boolean hasPlus = trimmed.startsWith("+");
        String digits = trimmed.replaceAll("\\D", "");

        if (digits.isEmpty()) throw new IllegalArgumentException("Phone must contain digits");

        return hasPlus ? "+" + digits : digits;
    }
}
