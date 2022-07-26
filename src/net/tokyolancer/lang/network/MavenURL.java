package net.tokyolancer.lang.network;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringJoiner;

public final class MavenURL implements Serializable {

    // "https://{repository}/{group_id}/{artifact_id}/{version}/{artifact_id}-{version}.jar"
    private static final String LINK_FORMAT = "%s/%s/%s/%s/%s-%s.jar";
    private static final int STANDARD_BUFFER_SIZE = 512;

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
        this.baseURL = LINK_FORMAT.formatted(repository.page(),
                groupId.replace(".", "/"), artifactId, version, artifactId, version);
        this.bufferSize = bufferSize;
    }

    public byte[] download() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedInputStream inStream = new BufferedInputStream(getURL().openStream() );
        byte[] buffer = new byte[bufferSize];
        int count;
        while ((count = inStream.read(buffer, 0, bufferSize) ) != -1) {
            outStream.write(buffer, 0, count);
            outStream.flush();
        }
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
