package ru.aslcraft.runtimeclassloader.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.aslcraft.runtimeclassloader.network.Dependency;
import ru.aslcraft.runtimeclassloader.util.pom.PomException;
import ru.aslcraft.runtimeclassloader.util.pom.PomObject;
import ru.aslcraft.runtimeclassloader.util.pom.PomReader;

public class XMLReader {
	@Deprecated
	public static List<Dependency> readDependencies(InputStream input) {
		final DocumentBuilderFactory dom = DocumentBuilderFactory.newInstance();

		final List<Dependency> dependencies = new ArrayList<>();

		try {
			dom.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			final DocumentBuilder build = dom.newDocumentBuilder();
			final Document doc = build.parse(input);
			doc.getDocumentElement().normalize();

			final int count = doc.getElementsByTagName("groupId").getLength();

			for (int i = 0 ; i < count ; i++) {
				final String groupId 		= doc.getElementsByTagName("groupId").item(i).getTextContent();
				final String artifactId 	= doc.getElementsByTagName("artifactId").item(i).getTextContent();
				final String version		= doc.getElementsByTagName("version").item(i).getTextContent();
				if (groupId == null || artifactId == null || version == null) continue;
				dependencies.add(new Dependency(groupId, artifactId, version));
			}

		} catch(final Exception e) { e.printStackTrace(); }

		return dependencies;

	}

	/**
	 * @param pom - xml file
	 * @param mvnURLs - list of maven repo urls
	 * @return - list of urls
	 * @author ZooMMaX
	 */
	public static HashMap<String,List<String>> dependencyURLs(File pom, List<String> mvnURLs){
		HashMap<String,List<String>> tmp = new HashMap<>();
		try {
			String str = new String(Files.readAllBytes(pom.toPath()), StandardCharsets.UTF_8);
			tmp = dependencyURLs(str, mvnURLs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tmp;
	}

	/**
	 * @param pom - xml string
	 * @param mvnURLs - list of maven repo urls
	 * @return - list of urls
	 * @author ZooMMaX
	 */
	public static HashMap<String,List<String>> dependencyURLs(String pom, List<String> mvnURLs){
		HashMap<String, List<String>> urlsHM = new HashMap<>();
		List<String> urlsStr = new ArrayList<>();
		PomReader pomReader = new PomReader(pom);
		PomObject pomObject = null;
		try {
			pomObject = pomReader.getTagData("dependencies");
		} catch (PomException e) {
			e.printStackTrace();
		}
		int x = 0;
		while (pomObject.containsTagName("dependency",x)){
			String groupId = null;
			try {
				groupId = pomObject.getTagData("dependency",x).getTagData("groupId").getValue().replace(".", "/")+"/";
			} catch (PomException e) {
				e.printStackTrace();
			}
			String artifactId = null;
			try {
				artifactId = pomObject.getTagData("dependency",x).getTagData("artifactId").getValue().replace(".", "/")+"/";
			} catch (PomException e) {
				e.printStackTrace();
			}
			String version = null;
			try {
				version = pomObject.getTagData("dependency",x).getTagData("version").getValue()+"/";
			} catch (PomException e) {
				version = null;
			}
			if (version == null){
				for (String url : mvnURLs){
					if (!url.endsWith("/")){
						url += "/";
					}
					url += groupId+artifactId+"maven-metadata.xml";
					StringBuilder metadata = new StringBuilder();
					try {
						URL u = new URL(url);
						HttpURLConnection connection = (HttpURLConnection)u.openConnection();
						connection.setRequestMethod("GET");
						connection.connect();
						int code = connection.getResponseCode();
						URLConnection uc = u.openConnection();
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
					} catch (IOException e) {
						e.printStackTrace();
					}
					PomReader metadataXml = new PomReader(metadata.toString());
					try {
						version = metadataXml.getTagData("versioning").getTagData("release").getValue()+"/";
						break;
					} catch (PomException e) {
						e.printStackTrace();
					}
				}
			}
			if (version.startsWith("${")){
				try {
					version = pomReader.getTagData("properties").getTagData(version.replace("${", "").replace("}/","")).getValue()+"/";
				} catch (PomException e) {
					try {
						StringBuilder metadata = new StringBuilder();
						for (String s : mvnURLs) {
							if (!s.endsWith("/")){
								s += "/";
							}
							String gId = pomReader.getTagData("parent").getTagData("groupId").getValue().replace(".", "/")+"/";
							String aId= pomReader.getTagData("parent").getTagData("artifactId").getValue().replace(".", "/")+"/";
							String ver = pomReader.getTagData("parent").getTagData("version").getValue();
							URL u = new URL(s+gId+aId+ver+"/"+aId.replace("/", "")+"-"+ver.replace("/", "")+".pom");
							HttpURLConnection connection = (HttpURLConnection)u.openConnection();
							connection.setRequestMethod("GET");
							connection.connect();
							int code = connection.getResponseCode();
							URLConnection uc = u.openConnection();
							BufferedReader in = new BufferedReader(
									new InputStreamReader(
											uc.getInputStream()));
							String inputLine;
							if (code == 200) {
								while ((inputLine = in.readLine()) != null) {
									metadata.append(inputLine);
								}
								in.close();
								break;
							}
						}
						PomReader metadataPOM = new PomReader(metadata.toString());
						version = metadataPOM.getTagData("properties").getTagData(version.replace("${", "").replace("}/","")).getValue()+"/";
					} catch (PomException | IOException ex) {
						e.printStackTrace();
					}
				}
			}
			String result = artifactId.replace("/", "")+"-"+version.replace("/", "");

			for (String mvn : mvnURLs) {
				urlsStr.add(mvn + groupId + artifactId + version + result);
			}
			x++;
		}
		List<String> dotPom = new ArrayList<>();
		List<String> dotJar = new ArrayList<>();
		for (String urls : urlsStr){
			dotPom.add(urls+".pom");
			dotJar.add(urls+".jar");
		}
		urlsHM.put("pom", dotPom);
		urlsHM.put("jar", dotJar);
		return urlsHM;
	}

}
