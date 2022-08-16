package ru.aslcraft.runtimeclassloader.util;

public class RuntimeUtil {

    // used for cache
    private static int runtimeVersion = -1;

    /**
     *
     * Retrieves the current version of the JVM.
     *
     * @return Runtime version in parsed view (8 - JAVA 1.8, 16 - JAVA 16 and etc.)
     */
    public static int getRuntimeVersion() {
        if (RuntimeUtil.runtimeVersion == -1) {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.") ) version = version.substring(2, 3);
            else {
                int dot = version.indexOf(".");
                if (dot != -1) version = version.substring(0, dot);
            }
            RuntimeUtil.runtimeVersion = Integer.parseInt(version);
        }
        return RuntimeUtil.runtimeVersion;
    }
}
