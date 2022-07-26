package net.tokyolancer.lang.reflect;

public class Offset {

    // -- Fields offsets -- //

    /*
     * java.lang.reflect.Method.java --> int modifiers;
     */
    public static long get_int_MODIFIERS() {
        switch (Reflection.getRuntimeVersion() ) {
            case 8:
                return 36L;
            case 16:
            case 17:
            default:
                return 32L;
        }
    }
}
