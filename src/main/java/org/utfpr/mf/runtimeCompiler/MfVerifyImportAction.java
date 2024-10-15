package org.utfpr.mf.runtimeCompiler;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import org.utfpr.mf.tools.CodeSession;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MfVerifyImportAction extends CodeSession implements IMfPreCompileAction{

    private IMfPreCompileAction _next;

    public MfVerifyImportAction() {
        this(null);
    }

    public MfVerifyImportAction(IMfPreCompileAction next) {
        super("MfVerifyImportAction");
        this._next = next;
    }

    @Override
    public String action(String className, String source) {
        JavaParser parser = new JavaParser();
        CompilationUnit unit = parser.parse(source).getResult().orElse(null);

        if(unit == null) {
            ERROR("CompilationUnit is null for " + className);
            return source;
        }


        List<ImportDeclaration> imports = unit.getImports();
        Set<String> importedTypes = new HashSet<>();
        for (ImportDeclaration importDecl : imports) {
            // Get the fully qualified name of each imported class
            importedTypes.add(importDecl.getNameAsString());
        }

        // Extract the fields of the class

        //TypeDeclaration<?> classDeclaration = unit.getType(0); // Get the first class/interface

        for(TypeDeclaration<?> classDeclaration : unit.getTypes()) {

            //List<FieldDeclaration> fields = classDeclaration.getFields();

            for(BodyDeclaration member : classDeclaration.getMembers()) {
                if(member.isClassOrInterfaceDeclaration()) {
                    var l = extracted(member.asClassOrInterfaceDeclaration().getFields(), importedTypes);
                    for(var x : l) {
                        unit.addImport(x);
                    }
                }
                else if(member.isFieldDeclaration()) {
                    var p = extracted(member.asFieldDeclaration(), importedTypes);
                    if(p != null) {
                        unit.addImport(p);
                    }
                }
            }
        }
        String result = unit.toString();
        return _next != null ? _next.action(className, result) : result;
    }

    private List<String> extracted(List<FieldDeclaration> fields, Set<String> importedTypes) {
        ArrayList<String> result = new ArrayList<>();
        for (FieldDeclaration field : fields) {
            var x = extracted(field, importedTypes);
            if(x != null) {
                result.add(x);
            }
        }
        return result;
    }

    private String extracted(FieldDeclaration field, Set<String> importedTypes) {
        Type fieldType = field.getElementType();
        String fieldTypeName = fieldType.asString();
        if (!isImported(fieldTypeName, importedTypes) && !isJavaLangType(fieldTypeName)) {
            if (fieldTypeName.equals("LocalDateTime")) {
                return "java.time.LocalDateTime";
            }
        }
        return null;
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
