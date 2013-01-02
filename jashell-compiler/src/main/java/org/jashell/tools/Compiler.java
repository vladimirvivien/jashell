/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jashell.tools;

import java.io.File;
import java.util.Map;

/**
 *
 * @author vvivien
 */
public abstract class Compiler {
    //abstract public void compile();
    //abstract public void compile(File[] files);
    //abstract public void compile(String code);
    
    /**
     * Compiles and run java code represented by Code parameter.
     * @param code - Java code to run
     */
    public void compile(String classFQN, String code){
        
    }
    
    public void compile(Map<String,CharSequence> code){
        
    }
    
    public void compile(File[] files){
        
    }
}