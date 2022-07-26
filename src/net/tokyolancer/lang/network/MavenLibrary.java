package net.tokyolancer.lang.network;

/**
 * Some needed libraries for plugins (like a MySQL or Oracle frameworks)
 */
public enum MavenLibrary {

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
        return this.repository;
    }

    public String groupId() {
        return this.groupId;
    }

    public String artifactId() {
        return this.artifactId;
    }

    public String version() {
        return this.version;
    }
}
