package net.tokyolancer.lang.network;

/**
 * All data fetched from: https://mvnrepository.com/repos
 */
@SuppressWarnings("unused")
public enum MavenRepository {

    Central("https://repo1.maven.org/maven2"),
    Sonatype("https://oss.sonatype.org/content/repositories/releases"),
    Atlassian("https://packages.atlassian.com/mvn/maven-atlassian-external"),
    Hortonworks("https://repo.hortonworks.com/content/repositories/releases"),
    SpringPlugins("https://repo.spring.io/plugins-release"),
    SpringLibM("https://repo.spring.io/libs-milestone"),
    JCenter("https://jcenter.bintray.com");

    private final String indexPage;

    MavenRepository(String indexPage) {
        this.indexPage = indexPage;
    }

    public String page() {
        return this.indexPage;
    }
}
