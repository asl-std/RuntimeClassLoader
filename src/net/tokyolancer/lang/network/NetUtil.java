package net.tokyolancer.lang.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class NetUtil {

    private NetUtil() { }

    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        return NetUtil.toByteArray(inputStream, 8192);
    }

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
}
