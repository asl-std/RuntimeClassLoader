package ru.aslcraft.runtimeclassloader.network;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Some needed libraries for plugins (like a MySQL or Oracle frameworks)
 */
public class MavenLibrary extends Dependency {

	public static MavenLibrary
	HTTPCLIENT 		= new MavenLibrary(MavenRepository.Central, "org.apache.httpcomponents", "httpclient", "4.5.13"),
	SQLITE_JDBC 	= new MavenLibrary(MavenRepository.Central, "org.xerial", "sqlite-jdbc", "3.39.2.0"),
	MYSQL_CONNECTOR = new MavenLibrary(MavenRepository.Central, "mysql", "mysql-connector-java", "8.0.30") {

		@Override
		protected void collectDependencies() {
			dependencies.add(new Dependency(MavenRepository.Central, "com.google.protobuf", "protobuf-java", "3.19.4"));
			dependencies.add(new Dependency(MavenRepository.Central, "com.oracle.oci.sdk", "oci-java-sdk-common", "2.14.1"));
		}

	},
	GSON 			= new MavenLibrary(MavenRepository.Central, "com.google.code.gson", "gson", "2.9.0"),
	HIKARICP 		= new MavenLibrary(MavenRepository.Central, "com.zaxxer", "HikariCP", "4.0.3"),
	JDA 			= new MavenLibrary(MavenRepository.Central, "net.dv8tion", "JDA", "5.0.0-alpha.17") {

		@Override
		protected void collectDependencies() {
			dependencies.add(new Dependency(MavenRepository.Central, "com.fasterxml.jackson.core", "jackson-core", "2.13.2"));
			dependencies.add(new Dependency(MavenRepository.Central, "com.fasterxml.jackson.core", "jackson-databind", "2.13.2.2"));
			dependencies.add(new Dependency(MavenRepository.Central, "net.sf.trove4j", "trove4j", "3.0.3"));
			dependencies.add(new Dependency(MavenRepository.Central, "", "", ""));
		}

	},
	ORG_JSON 		= new MavenLibrary(MavenRepository.Central, "org.json", "json", "20220320"),
	ASM 			= new MavenLibrary(MavenRepository.Central, "org.ow2.asm", "asm", "9.3");


	protected List<Dependency> dependencies = new ArrayList<>();

	MavenLibrary(MavenRepository repository, String groupId, String artifactId, String version) {
		super(repository, groupId, artifactId, version);

		collectDependencies();
	}

	protected void collectDependencies() {}

	public ImmutableList<Dependency> getDependencies() {
		return ImmutableList.copyOf(dependencies);
	}

}
