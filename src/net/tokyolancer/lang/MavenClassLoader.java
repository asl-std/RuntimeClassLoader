package net.tokyolancer.lang;

public class MavenClassLoader extends ClassLoader {

    public MavenClassLoader() {
        super(ClassLoader.getSystemClassLoader() );
    }


}
