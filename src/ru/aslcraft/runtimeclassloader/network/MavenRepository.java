package ru.aslcraft.runtimeclassloader.network;

/**
 * All data fetched from: https://mvnrepository.com/repos
 */
public enum MavenRepository {

	Central("https://repo1.maven.org/maven2"),
	Sonatype("https://oss.sonatype.org/content/repositories/releases"),
	Atlassian("https://packages.atlassian.com/mvn/maven-atlassian-external"),
	JCenter("https://jcenter.bintray.com");

	private final String indexPage;

	MavenRepository(String indexPage) {
		this.indexPage = indexPage;
	}

	public String page() {
		return indexPage;
	}
}
