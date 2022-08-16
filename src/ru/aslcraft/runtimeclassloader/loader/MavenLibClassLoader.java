package ru.aslcraft.runtimeclassloader.loader;

import ru.aslcraft.runtimeclassloader.network.Dependency;
import ru.aslcraft.runtimeclassloader.network.MavenPom;
import ru.aslcraft.runtimeclassloader.util.XMLReader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenLibClassLoader {

    private final Set<Dependency> dependencies = new HashSet<>();
    private final Dependency dependency;

    public MavenLibClassLoader(Dependency dependency) throws IOException {
        this.dependency = dependency;
    }

    public void resolveDependencies() throws IOException {
        for (Dependency tmp : this.getDependencies(dependency) )
            System.out.println(this.getDependencies(tmp) );
    }

    private List<Dependency> getDependencies(Dependency dependency) throws IOException {
        MavenPom pom = new MavenPom(dependency);
        System.out.printf("----- Checking pom: %s -----\n", pom.page() );
        return XMLReader.readDependencies(Files.newInputStream(pom.getFile().toPath() ) );
    }
}
