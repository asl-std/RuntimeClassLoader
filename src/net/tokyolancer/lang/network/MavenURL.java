package net.tokyolancer.lang.network;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringJoiner;

public final class MavenURL implements Serializable {

    // "{repository}/{group_id}/{artifact_id}/{version}/{artifact_id}-{version}.jar"
    private static final String LINK_FORMAT = "%s/%s/%s/%s/%s-%s.jar";
    private static final int STANDARD_BUFFER_SIZE = 1024 * 3;

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
        this.baseURL = String.format(LINK_FORMAT, repository.page(),
                groupId.replace(".", "/"), artifactId, version, artifactId, version);
//        this.baseURL = LINK_FORMAT.formatted(repository.page(),
//                groupId.replace(".", "/"), artifactId, version, artifactId, version);
        this.bufferSize = bufferSize;
    }

    public byte[] download() throws IOException {
        long millis = System.currentTimeMillis();
        byte[] data = this.download0();
        System.out.println("Downloaded in " + (System.currentTimeMillis() - millis) + " ms");
        return data;
    }

    private byte[] download0() throws IOException {
        return NetUtil.download(getURL().openStream() );
    }

    public URL getURL() throws MalformedURLException {
        if (currURL == null) return this.currURL = new URL(baseURL);
        return this.currURL;
    }

    public static MavenURL fromMavenLibrary(MavenLibrary library) {
        return new MavenURL(library.repository(), library.groupId(), library.artifactId(), library.version() );
    }
}
