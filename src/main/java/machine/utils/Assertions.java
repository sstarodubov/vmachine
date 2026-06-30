package machine.utils;

public class Assertions {

    public static void require(final boolean exp, final String msg) {
        if (!exp) {
           throw new IllegalStateException(msg);
        }
    }
}
