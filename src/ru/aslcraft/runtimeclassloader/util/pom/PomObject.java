package ru.aslcraft.runtimeclassloader.util.pom;

import java.util.LinkedHashMap;

/**
 * @author ZooMMaX
 */
public class PomObject {
    private LinkedHashMap<String, PomObject> data = new LinkedHashMap<>();
    private String tagName = "";
    private String value = "";



    public PomObject getTagData(String tagName) throws PomException{
        if (containsTagName(tagName)) {
            return data.get(tagName + 0);
        }else {
            throw new PomException(tagName, 0);
        }
    }

    public PomObject getTagData(String tagName, int tagIndex) throws PomException {
        if (containsTagName(tagName)) {
            return data.get(tagName + tagIndex);
        }else {
            throw new PomException(tagName, tagIndex);
        }
    }

    public boolean containsTagName(String tagName){
        return data.containsKey(tagName+0);

    }

    public LinkedHashMap<String, PomObject> getData() {
        return data;
    }

    public boolean containsTagName(String tagName, int tagIndex){
        return data.containsKey(tagName+tagIndex);

    }

    public void setData(String key, PomObject pomObject){
        int x = 0;
        while (data.containsKey(key+x)){
            x++;
        }
        data.put(key+x, pomObject);
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
