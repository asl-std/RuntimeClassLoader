package ru.aslcraft.runtimeclassloader.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ru.aslcraft.runtimeclassloader.network.Dependency;

public class XMLReader {

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

}
