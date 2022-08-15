package ru.aslcraft.runtimeclassloader.network;

import ru.aslcraft.runtimeclassloader.util.FileUtil;
import ru.aslcraft.runtimeclassloader.util.NetUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MavenPom {

    private static final String LINK_FORMAT = "%s/%s/%s/%s/%s-%s.pom";

    private final String baseURL;

    public MavenPom(Dependency dependency) {
        this.baseURL = String.format(LINK_FORMAT,
                dependency.repository().page(),
                dependency.groupId().replace(".", "/"),
                dependency.artifactId(),
                dependency.version(),
                dependency.artifactId(),
                dependency.version() );
    }

    public File getFile() throws IOException {
        return FileUtil.toFile(
                NetUtil.toByteArray(new URL(baseURL).openConnection() )
        );
    }

    public String page() {
        return this.baseURL;
    }
}
