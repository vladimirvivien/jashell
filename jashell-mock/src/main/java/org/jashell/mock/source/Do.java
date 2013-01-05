package org.jashell.mock.source;

/**
 * Sample class used in mocking and testing compiler.
 * @author vladimir.vivien
 */
public class Do {
    private String task;
    public void something(String t){
        task = t;
    }
    public String getSomething(){
        return task;
    }
}
