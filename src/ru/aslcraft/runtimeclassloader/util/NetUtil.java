package ru.aslcraft.runtimeclassloader.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.ByteBuffer;

public final class NetUtil {

    private NetUtil() { }

    /**
     * Converts the current stream into an array of bytes.
     * This method is necessary for compatibility with previous versions of Java.
     *
     * @param inputStream Exact input stream
     * @return Byte array
     * @throws IOException If error thrown
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        return NetUtil.toByteArray(inputStream, 8192);
    }

    /**
     * Converts the current stream into an array of bytes.
     * This method is necessary for compatibility with previous versions of Java.
     *
     * @param inputStream Exact input stream
     * @param directSize Size of temp buffer
     * @return Byte array
     * @throws IOException If error thrown
     */
    public static byte[] toByteArray(InputStream inputStream, int directSize) throws IOException {
        // The HttpURLConnection will take care of all the de-chunking for you.
        // Just copy the bytes until end of stream.
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[directSize];
        while ((nRead = inputStream.read(data, 0, data.length) ) != -1)
            outStream.write(data, 0, nRead);
        outStream.close();
        inputStream.close();
        return outStream.toByteArray();
    }

    public static byte[] toByteArray(URLConnection connection) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(connection.getContentLength() );
        InputStream inStream = connection.getInputStream();
        int nRead;
        while ((nRead = inStream.read() ) != -1)
            buffer.put((byte) nRead);
        return buffer.array();
    }
}
