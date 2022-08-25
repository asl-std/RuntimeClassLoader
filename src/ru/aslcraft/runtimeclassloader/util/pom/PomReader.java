package ru.aslcraft.runtimeclassloader.util.pom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * @author ZooMMaX
 */
public class PomReader extends PomObject{
    List<String> dataIn;

    public PomReader(List<String> dataIn){
        this.dataIn = dataIn;
        read();
    }

    public PomReader(String xml){
        this.dataIn = xmlCleaner(xml);
        read();
    }

    public PomReader(File xml){
        String xmlStr = "";
        try {
            xmlStr = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.dataIn = xmlCleaner(xmlStr);
        read();
    }

    private LinkedList<String> xmlCleaner(String xml){
        xml = xml.replaceAll("\\<project[^*]+\">", "<root>").replace("</project>", "</root>");
        xml = xml.replaceAll("\\<metadata[^*]+\">", "<root>").replace("</metadata>", "</root>");
        if (xml.contains("<root")) {
            xml = xml.substring(xml.indexOf("<root"));
        }

        String clean = xml.replaceAll("\n", "");
        String[] tmp = clean.replace(">", ">`").replace("<", "`<").split("`");
        LinkedList<String> out = new LinkedList<>( Arrays.asList(tmp));
        return out;
    }

    private void read(){
        LinkedList<String> anyLevel = new LinkedList<>();
        List<HashMap<String, Object>> forPomObject = new ArrayList<>();
        String openTag = "";
        boolean tag = false;
        boolean tag2 = false;
        String openTag2 = "";
        for (String s : dataIn){
            if (!s.equals("") && !s.contains("<!") && !s.contains("<?")) {
                s = s.trim();
                if (s.contains("<") && !s.contains("/") && tag && !tag2 && !s.contains(openTag)) {
                    openTag2 = s.replace("<", "").replace(">", "");
                    tag2 = true;
                }
                if (tag2) {
                    anyLevel.add(s);
                }
                if (s.contains("<") && s.contains("/") && s.replace("<","").replace("/", "").replace(">","").equals(openTag2)) {
                    HashMap<String, Object> tmpHashMap = new HashMap<>();
                    LinkedList<String> tmpList = new LinkedList<>(anyLevel);
                    tmpHashMap.put("tag", openTag2);
                    tmpHashMap.put("data", tmpList);
                    forPomObject.add(tmpHashMap);
                    anyLevel.clear();
                    tag2 = false;
                    openTag2 = "";
                }

                if (s.contains("<") && !s.contains("/") && !tag) {
                    openTag = s.replace("<", "").replace(">", "");
                    tag = true;
                    this.setTagName(openTag);
                }
                if (s.contains("<") && s.contains("/") && tag && s.contains(openTag)) {
                    openTag = "";
                    tag = false;
                }

                if (!s.contains("<") && !s.contains("/") && tag && !tag2) {
                    this.setTagName(openTag);
                    this.setValue(s);
                }
            }
        }
        for (HashMap<String, Object> i : forPomObject){
            String tagn = (String) i.get("tag");
            LinkedList<String> data = (LinkedList<String>) i.get("data");
            this.setData(tagn, new PomReader(data));
        }
    }
}
