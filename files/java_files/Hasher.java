public class Hasher {

    public static short MAX_SSN_LEN = 12;

    /**
     * Computes a shash digest of a String.
     * @return The result is bound between 0-255
     */
    public static short hashSSN(String s) {
        int result = 5381;

        for (int i = 0; i < MAX_SSN_LEN; i++) {
            result = ((result << 5) + result) + (int)s.charAt(i);
        }

        return (short) (result % 256);
    }
}
