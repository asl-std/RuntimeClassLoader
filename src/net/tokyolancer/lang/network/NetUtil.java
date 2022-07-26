package net.tokyolancer.lang.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class NetUtil {

    private NetUtil() { }

    public static byte[] download(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length) ) != -1)
            buffer.write(data, 0, nRead);
        buffer.flush();
        buffer.close();
        return buffer.toByteArray();
    }
}
