package ru.aslcraft.runtimeclassloader.network;

public class Dependency {

	private final MavenRepository repository;
	private final String groupId;
	private final String artifactId;
	private final String version;

	public Dependency(String groupId, String artifactId, String version) {
		this(MavenRepository.Central, groupId,artifactId,version);
	}

	public Dependency(MavenRepository repository, String groupId, String artifactId, String version) {
		this.repository = repository;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	public MavenRepository repository() {
		return repository;
	}

	public String groupId() {
		return groupId;
	}

	public String artifactId() {
		return artifactId;
	}

	public String version() {
		return version;
	}

}
