package net.tokyolancer.lang.network;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringJoiner;

public final class MavenURL implements Serializable {

    private static final int STANDARD_BUFFER_SIZE = 2048;

    private final String baseURL;
    private final int bufferSize;

    private URL currURL;

    public MavenURL(MavenRepository repository,
                    String groupId,
                    String artifactId,
                    String version) {
        this(repository, groupId, artifactId, version, STANDARD_BUFFER_SIZE);
    }

    public MavenURL(MavenRepository repository,
                    String groupId,
                    String artifactId,
                    String version,
                    int bufferSize) {
        StringJoiner sj = new StringJoiner(File.separator);
        sj.add(repository.page() );
        sj.add(groupId.replace(".", "/") );
        sj.add(artifactId);
        sj.add(version);

        StringJoiner sj2 = new StringJoiner("-");
        sj2.add(artifactId);
        sj2.add(version);

        sj.merge(sj2);

        this.baseURL = sj.toString().replace("\\", "/") + ".jar";
        this.bufferSize = bufferSize;
    }

    public byte[] download() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedInputStream inStream = new BufferedInputStream(getURL().openStream() );
        byte[] buffer = new byte[bufferSize];
        int count;
        while ((count = inStream.read(buffer, 0, bufferSize)) != -1) {
            outStream.write(buffer, 0, count); // write remaining data
            outStream.flush(); // clear temp system buffer
        }
        // close all streams
        outStream.flush();
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public URL getURL() throws MalformedURLException {
        if (currURL == null) return this.currURL = new URL(baseURL);
        return this.currURL;
    }

    public static MavenURL fromMavenLibrary(MavenLibrary library) {
        return new MavenURL(library.repository(), library.groupId(), library.artifactId(), library.version() );
    }
}
