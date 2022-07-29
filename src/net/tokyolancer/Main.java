package net.tokyolancer;

import net.tokyolancer.lang.exp.MavenClassLoader;
import net.tokyolancer.lang.network.MavenLibrary;
import net.tokyolancer.lang.network.MavenURL;

public class Main {

    public static void main(String[] args) throws Throwable {
        MavenClassLoader loader = new MavenClassLoader(MavenURL.fromMavenLibrary(MavenLibrary.ASM) );
        loader.loadClasses();
    }
}
