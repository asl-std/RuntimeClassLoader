package ru.aslcraft.tests;

import ru.aslcraft.runtimeclassloader.util.XMLReader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Throwable {
		List<String> url = new ArrayList<>();
		StringBuilder metadata = new StringBuilder();
		url.add("https://repo1.maven.org/maven2/");
		URL website = new URL("https://repo1.maven.org/maven2/com/oracle/oci/sdk/oci-java-sdk-common/2.14.1/oci-java-sdk-common-2.14.1.pom");
		HttpURLConnection connection = (HttpURLConnection)website.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		int code = connection.getResponseCode();
		URLConnection uc = website.openConnection();
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						uc.getInputStream()));
		String inputLine;
		if (code == 200) {
			while ((inputLine = in.readLine()) != null) {
				metadata.append(inputLine);
			}
			in.close();
		}
		List<String> urlsPOM = XMLReader.dependencyURLs(metadata.toString(), url).get("pom");
		for (String s : urlsPOM){
			System.out.println(s);
		}
		List<String> urlsJAR = XMLReader.dependencyURLs(metadata.toString(), url).get("jar");
		for (String s : urlsJAR){
			System.out.println(s);
		}
		/*for (final MavenLibrary lib : MavenLibrary.values()) {
			final MavenClassLoader loader = new MavenClassLoader(MavenURL.fromMavenLibrary(lib), true);

			System.out.println("Successfully loaded " + lib.groupId() + "." + lib.artifactId());

			System.out.println(loader.loadClasses());
		}*/



		//final MavenLibrary lib = MavenLibrary.MYSQL_CONNECTOR;
//
		//new MavenLibClassLoader(lib).resolveDependencies();

//		final JarClassLoader loader = new JarClassLoader(MavenURL.fromDependency(MavenLibrary.GSON).download() );
//		System.out.println(loader.loadClasses() );

//
//		System.out.println(lib.getDependencies().size() );
//
//		final MavenClassLoader loader = new MavenClassLoader(lib);
//
//		System.out.println("Successfully loaded " + lib.groupId() + "." + lib.artifactId());
//
//		System.out.println(loader.loadClasses());

		/*final Reflection reflection = ReflectionFactory.createReflection();
		System.out.println(Arrays.toString(reflection.getClass().getDeclaredFields()));
		System.out.println(Arrays.toString(reflection.getClass().getDeclaredMethods()));*/
	}

	public static void bumpPomToDependency() {

	}
}
