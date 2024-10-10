package org.utfpr.mf.runtimeCompiler;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;

import javax.naming.OperationNotSupportedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MfVerifyImportAction implements IMfPreCompileAction{

    private IMfPreCompileAction _next;

    public MfVerifyImportAction() {
        this(null);
    }

    public MfVerifyImportAction(IMfPreCompileAction next) {
        this._next = next;
    }

    @Override
    public String action(String className, String source) {
        JavaParser parser = new JavaParser();
        CompilationUnit unit = parser.parse(source).getResult().orElse(null);

        if(unit == null) {
            return source;
        }

        List<ImportDeclaration> imports = unit.getImports();
        Set<String> importedTypes = new HashSet<>();
        for (ImportDeclaration importDecl : imports) {
            // Get the fully qualified name of each imported class
            importedTypes.add(importDecl.getNameAsString());
        }

        // Extract the fields of the class
        TypeDeclaration<?> classDeclaration = unit.getType(0);  // Get the first class/interface
        List<FieldDeclaration> fields = classDeclaration.getFields();

        for (FieldDeclaration field : fields) {
            // Extract the type of each field
            Type fieldType = field.getElementType();
            String fieldTypeName = fieldType.asString();

            // Check if the type is already imported or is in java.lang (implicitly available)
            if (!isImported(fieldTypeName, importedTypes) && !isJavaLangType(fieldTypeName)) {
                if (fieldTypeName.equals("LocalDateTime")) {// Add the import for LocalDateTime
                    unit.addImport("java.time.LocalDateTime");
                }
            }
        }
        String result = unit.toString();
        return _next != null ? _next.action(className, result) : result;
    }

    @Override
    public void setNext(IMfPreCompileAction next) throws OperationNotSupportedException {
        this._next = next;
    }

    private static boolean isImported(String fieldTypeName, Set<String> importedTypes) {
        // Check if the field's type or its package is in the imported list
        return importedTypes.stream().anyMatch(imported -> imported.endsWith(fieldTypeName));
    }

    private static boolean isJavaLangType(String fieldTypeName) {
        try {
            // Check if the field type is in java.lang (implicitly imported)
            Class.forName("java.lang." + fieldTypeName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
