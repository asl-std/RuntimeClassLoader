package ru.aslcraft.runtimeclassloader.util.pom;

/**
 * @author ZooMMaX
 */
public class PomException extends Exception{
    public PomException(String tagName, int tagIndex){
        super("Tag \""+tagName+"\""+" index \""+tagIndex+"\" not found");
    }
}
