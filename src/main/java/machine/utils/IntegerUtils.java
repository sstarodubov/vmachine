package machine.utils;

public class IntegerUtils {

    public static int parseInt(final String s) {
        if (s.startsWith("0x")) {
            return Integer.parseInt(s.substring(2), 16);
        }
        return Integer.parseInt(s);
    }
}
