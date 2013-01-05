package org.jashell.tools;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * A container class that provides a facade for Diagnostic<JavaObject>.
 * @author vvivien
 */
public class CompilationDiagnostic{
    private Diagnostic<JavaFileObject> diag;
    public CompilationDiagnostic(Diagnostic<JavaFileObject> diag){
        this.diag = diag;
    }

    public Diagnostic getDiagnositc() {
        return diag;
    }
    
    public String getKind() {
        return diag.getKind().toString();
    }

    public long getPosition() {
        return diag.getPosition();
    }

    public long getStartPosition() {
        return diag.getStartPosition();
    }

    public long getEndPosition() {
        return diag.getEndPosition();
    }

    public long getLineNumber() {
        return diag.getLineNumber();
    }

    public long getColumnNumber() {
        return diag.getColumnNumber();
    }

    public String getCode() {
        return diag.getCode();
    }

    /**
     * Returns Diagnostic.getMessage(null);
     * @return 
     */
    public String getMessage() {
        return diag.getMessage(null);
    }
    
    @Override
    public String toString() {
        return diag.toString();
    }
    
}
