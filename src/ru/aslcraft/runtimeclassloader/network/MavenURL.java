package ru.aslcraft.runtimeclassloader.network;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("serial")
public final class MavenURL implements Serializable {

	// Used only for debug
	private static final boolean isDebugging = true;

	// "{repository}/{group_id}/{artifact_id}/{version}/{artifact_id}-{version}.jar"
	private static final String LINK_FORMAT = "%s/%s/%s/%s/%s-%s.jar";

	private final String baseURL;

	private URL currURL;

	public MavenURL(MavenRepository repository,
			String groupId,
			String artifactId,
			String version) {
		this.baseURL = String.format(LINK_FORMAT, repository.page(),
				groupId.replace(".", "/"), artifactId, version, artifactId, version);
	}

	public byte[] download() throws IOException {
		final long millis = System.currentTimeMillis();
		final byte[] data = this.download0();

		if (MavenURL.isDebugging) {
			System.out.println("Downloaded in " + (System.currentTimeMillis() - millis) + " ms");
		}

		return data;
	}

	private byte[] download0() throws IOException {
		return NetUtil.toByteArray(getURL().openConnection() );
	}

	public URL getURL() throws MalformedURLException {
		if (currURL == null) return currURL = createURL();
		return currURL;
	}

	public URL createURL() throws MalformedURLException {
		return new URL(baseURL);
	}

	public static MavenURL fromDependency(Dependency dependency) {
		return new MavenURL(dependency.repository(), dependency.groupId(), dependency.artifactId(), dependency.version() );
	}
}
