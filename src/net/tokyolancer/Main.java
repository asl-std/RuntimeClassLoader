package net.tokyolancer;

import net.tokyolancer.lang.network.AsyncURL;
import net.tokyolancer.lang.network.MavenRepository;
import net.tokyolancer.lang.network.MavenURL;
import net.tokyolancer.lang.network.NetUtil;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Throwable {
        MavenURL mavenURL = new MavenURL(MavenRepository.Central,"javax", "javaee-web-api", "8.0.1");
//        MavenRTClassLoader classLoader = new MavenRTClassLoader(mavenURL, true);
//        classLoader.loadClasses();

//        MavenURL mavenURL = MavenURL.fromMavenLibrary(MavenLibrary.ASM);
//        MavenRTClassLoader classLoader1 = new MavenRTClassLoader(mavenURL);
        URL url = mavenURL.createURL();
        URLConnection connection = url.openConnection();
//        connection.setRequestProperty("Accept-Ranges", "bytes");
//        connection.setRequestProperty("Range", "bytes=0-399"); // only 400 bytes
        InputStream stream = connection.getInputStream();
        long millis = System.currentTimeMillis();
        byte[] bytes = NetUtil.toByteArray(stream);
        System.out.println("End in: " + (System.currentTimeMillis() - millis) );
        System.out.println(bytes.length);

        AsyncURL url1 = new AsyncURL(mavenURL.createURL(), 1542007);
        millis = System.currentTimeMillis();
        byte[] bytes1 = url1.readAllBytes();
        System.out.println("End in: " + (System.currentTimeMillis() - millis) );
        System.out.println(bytes1.length);
        // true
        System.out.println(Arrays.equals(bytes, bytes1) );

//        ExecutorService service = Executors.newCachedThreadPool();
//        Future<byte[]> task1 = service.submit(new RangedBytes(url, 1, 399) );
//        System.out.println(task1.get().length );
//
//        service.shutdown();


//        classLoader1.loadClasses();
//
//        Class<?> clazz = Class.forName("org.objectweb.asm.signature.SignatureWriter");
//
//        System.out.println(Reflection.lookup().getObject(ClassLoader.getSystemClassLoader(), 48) );
    }
}
