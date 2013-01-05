package org.jashell.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

/**
 * Handles diagnostic objects emitted by the underlying compiler tool.
 * @author vvivien
 */
public class CompilationDiagnosticHandler implements DiagnosticListener{
    private List<CompilationDiagnostic> diags = Collections.synchronizedList(new ArrayList<CompilationDiagnostic>());
    public void report(Diagnostic diagnostic) {
        diags.add(new CompilationDiagnostic(diagnostic));
    }
    
    public List<CompilationDiagnostic> getCompilationDiagnostics() {
        return diags;
    }
}
