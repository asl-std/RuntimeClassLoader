package ru.aslcraft.api.dependency.loader.network;

/**
 * Some needed libraries for plugins (like a MySQL or Oracle frameworks)
 */
public enum MavenLibrary {

	HTTPCLIENT(MavenRepository.Central, "org.apache.httpcomponents", "httpclient", "4.5.13"),
	SQLITE_JDBC(MavenRepository.Central, "org.xerial", "sqlite-jdbc", "3.39.2.1-SNAPSHOT"),
	MYSQL_CONNECTOR(MavenRepository.Central, "mysql", "mysql-connector-java", "8.0.30"),
	GSON(MavenRepository.Central, "com.google.code.gson", "gson", "2.9.0"),
	HIKARICP(MavenRepository.Central, "com.zaxxer", "HikariCP", "4.0.3"),
	JDA(MavenRepository.Central, "net.dv8tion", "JDA", "5.0.0-alpha.17"),
	ORG_JSON(MavenRepository.Central, "org.json", "json", "20220320"),
	ASM(MavenRepository.Central, "org.ow2.asm", "asm", "9.3");

	private final MavenRepository repository;
	private final String groupId;
	private final String artifactId;
	private final String version;

	MavenLibrary(MavenRepository repository, String groupId, String artifactId, String version) {
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
