package com.codeglimmer.parser;

import com.codeglimmer.model.*;
import com.codeglimmer.model.Class;
import com.codeglimmer.model.Enum;
import com.codeglimmer.model.Record;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaCodeParser implements CodeParser {

    @Override
    public ParsedCode parse(Path projectPath) throws IOException {
        validateProjectPath(projectPath);
        configureJavaParser(projectPath);

        ParsedCode parsedCode = new ParsedCode();
        parseJavaFiles(projectPath, parsedCode);
        java.io.File pomFile = getPOMFile(projectPath);
        parsedCode.setDependencies(parseDependencies(pomFile));
        parsedCode.setProjectMetadata(parseProjectMetadata(pomFile));
        parsedCode.setLanguage(extractLanguage(pomFile));
        parsedCode.setProjectName(extractProjectName(pomFile));
        parseProjectMetadata(pomFile);
        calculateComplexityMetrics(projectPath, parsedCode);

        return parsedCode;
    }

    private void validateProjectPath(Path projectPath) throws FileNotFoundException {
        if (Files.notExists(projectPath)) {
            throw new FileNotFoundException("The directory at path " + projectPath + " does not exist.");
        }
    }

    private java.io.File getPOMFile(Path projectPath){
        java.io.File pomFile = projectPath.resolve("pom.xml").toFile();
        return pomFile;
    }

    private void configureJavaParser(Path projectPath) throws IOException {
        StaticJavaParser.getConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17_PREVIEW);

        java.io.File pomFile = projectPath.resolve("pom.xml").toFile();
        String javaVersion = parseJavaVersion(pomFile);
        if (javaVersion != null) {
            StaticJavaParser.getConfiguration().setLanguageLevel(parseLanguageLevel(javaVersion));
        }
    }

    private void parseJavaFiles(Path projectPath, ParsedCode parsedCode) throws IOException {
        try (Stream<Path> paths = Files.walk(projectPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> parseAndPopulateFiles(path, parsedCode));
        }
    }

    private void parseAndPopulateFiles(Path path, ParsedCode parsedCode) {
        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(path);
            com.codeglimmer.model.File file = new com.codeglimmer.model.File();
            file.setName(path.getFileName().toString());
            file.setClasses(parseClasses(compilationUnit));
            file.setInterfaces(parseInterfaces(compilationUnit));
            file.setEnums(parseEnums(compilationUnit));
            file.setRecords(parseRecords(compilationUnit));
            file.setAnnotationClasses(parseAnnotationClasses(compilationUnit));
            parsedCode.getFiles().add(file);
        } catch (IOException e) {
            e.printStackTrace(); // Consider a better exception handling strategy
        }
    }

    private List<Class> parseClasses(CompilationUnit compilationUnit) {
        List<Class> classes = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classDeclarations = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration classDeclaration : classDeclarations) {
            if (!classDeclaration.isInterface()) {
                Class classModel = new Class();
                classModel.setName(classDeclaration.getNameAsString());
                classModel.setAnnotations(parseAnnotations(classDeclaration.getAnnotations()));

                List<Method> methods = new ArrayList<>();
                classDeclaration.getMethods().forEach(method -> methods.add(parseMethod(method)));
                classModel.setMethods(methods);

                List<Field> fields = new ArrayList<>();
                classDeclaration.getFields().forEach(field -> fields.add(parseField(field)));
                classModel.setFields(fields);

                classes.add(classModel);
            }
        }

        return classes;
    }

    private List<Interface> parseInterfaces(CompilationUnit compilationUnit) {
        List<Interface> interfaces = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> interfaceDeclarations = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration interfaceDeclaration : interfaceDeclarations) {
            if (interfaceDeclaration.isInterface()) {
                Interface interfaceModel = new Interface();
                interfaceModel.setName(interfaceDeclaration.getNameAsString());
                interfaceModel.setAnnotations(parseAnnotations(interfaceDeclaration.getAnnotations()));

                List<Method> methods = new ArrayList<>();
                interfaceDeclaration.getMethods().forEach(method -> methods.add(parseMethod(method)));
                interfaceModel.setMethods(methods);

                interfaces.add(interfaceModel);
            }
        }

        return interfaces;
    }

    private List<Enum> parseEnums(CompilationUnit compilationUnit) {
        List<Enum> enums = new ArrayList<>();
        List<EnumDeclaration> enumDeclarations = compilationUnit.findAll(EnumDeclaration.class);

        for (EnumDeclaration enumDeclaration : enumDeclarations) {
            Enum enumModel = new Enum();
            enumModel.setName(enumDeclaration.getNameAsString());
            enumModel.setAnnotations(parseAnnotations(enumDeclaration.getAnnotations()));

            List<String> values = new ArrayList<>();
            enumDeclaration.getEntries().forEach(entry -> values.add(entry.getNameAsString()));
            enumModel.setValues(values);

            List<Method> methods = new ArrayList<>();

            enums.add(enumModel);
        }

        return enums;
    }

    private List<Record> parseRecords(CompilationUnit compilationUnit) {
        List<Record> records = new ArrayList<>();
        List<RecordDeclaration> recordDeclarations = compilationUnit.findAll(RecordDeclaration.class);

        for (RecordDeclaration recordDeclaration : recordDeclarations) {
            Record recordModel = new Record();
            recordModel.setName(recordDeclaration.getNameAsString());
            recordModel.setAnnotations(parseAnnotations(recordDeclaration.getAnnotations()));

            List<Method> methods = new ArrayList<>();
            recordDeclaration.getMethods().forEach(method -> methods.add(parseMethod(method)));

            records.add(recordModel);
        }

        return records;
    }

    private List<AnnotationClass> parseAnnotationClasses(CompilationUnit compilationUnit) {
        List<AnnotationClass> annotationClasses = new ArrayList<>();
        List<AnnotationDeclaration> annotationDeclarations = compilationUnit.findAll(AnnotationDeclaration.class);

        for (AnnotationDeclaration annotationDeclaration : annotationDeclarations) {
            AnnotationClass annotationClass = new AnnotationClass();
            annotationClass.setName(annotationDeclaration.getNameAsString());
            annotationClass.setDescription(annotationDeclaration.getComment().map(Comment::getContent).orElse(null));
            annotationClass.setAnnotations(parseAnnotations(annotationDeclaration.getAnnotations()));
            annotationClass.setElements(parseAnnotationElements(annotationDeclaration));

            annotationClasses.add(annotationClass);
        }

        return annotationClasses;
    }

    private List<Annotation> parseAnnotations(List<AnnotationExpr> annotationExprs) {
        List<Annotation> annotations = new ArrayList<>();
        for (AnnotationExpr annotationExpr : annotationExprs) {
            Annotation annotation = new Annotation();
            annotation.setName(annotationExpr.getNameAsString());
            if (annotationExpr.isNormalAnnotationExpr()) {
                annotation.setParameters(parseAnnotationValues((NormalAnnotationExpr) annotationExpr));
            }
            annotations.add(annotation);
        }
        return annotations;
    }

    private Map<String, String> parseAnnotationValues(NormalAnnotationExpr annotationExpr) {
        Map<String, String> values = new HashMap<>();
        for (MemberValuePair pair : annotationExpr.getPairs()) {
            values.put(pair.getNameAsString(), pair.getValue().toString());
        }
        return values;
    }

    private List<AnnotationElement> parseAnnotationElements(AnnotationDeclaration annotationDeclaration) {
        List<AnnotationElement> annotationElements = new ArrayList<>();
        NodeList<BodyDeclaration<?>> bodyDeclarations = annotationDeclaration.getMembers();

        for (BodyDeclaration<?> bodyDeclaration : bodyDeclarations) {
            if (bodyDeclaration instanceof AnnotationMemberDeclaration) {
                AnnotationMemberDeclaration memberDeclaration = (AnnotationMemberDeclaration) bodyDeclaration;
                AnnotationElement annotationElement = new AnnotationElement();
                annotationElement.setName(memberDeclaration.getNameAsString());
                annotationElement.setType(memberDeclaration.getType().asString());
                annotationElement.setDescription(memberDeclaration.getComment().map(Comment::getContent).orElse(null));
                annotationElement.setDefaultValue(memberDeclaration.getDefaultValue().map(Expression::toString).orElse(null));

                annotationElements.add(annotationElement);
            }
        }

        return annotationElements;
    }

    private Method parseMethod(MethodDeclaration methodDeclaration) {
        Method method = new Method();
        method.setName(methodDeclaration.getNameAsString());
        method.setReturnType(methodDeclaration.getType().asString());
        method.setAnnotations(parseAnnotations(methodDeclaration.getAnnotations()));
        method.setCode(methodDeclaration.toString());

        // Parsing method parameters
        List<String> parameters = new ArrayList<>();
        for (Parameter parameter : methodDeclaration.getParameters()) {
            parameters.add(parameter.getType().asString() + " " + parameter.getName().asString());
        }
        method.setParameters(parameters);

        return method;
    }

    private Field parseField(FieldDeclaration fieldDeclaration) {
        Field field = new Field();
        field.setName(fieldDeclaration.getVariable(0).getNameAsString());
        field.setType(fieldDeclaration.getElementType().asString());
        field.setDefaultValue(fieldDeclaration.getVariable(0).getInitializer().map(init -> init.toString()).orElse(null));
        field.setAnnotations(parseAnnotations(fieldDeclaration.getAnnotations()));

        return field;
    }

    private String parsePackage(CompilationUnit compilationUnit) {
        return compilationUnit.getPackageDeclaration()
                .map(packageDeclaration -> packageDeclaration.getName().asString())
                .orElse(null);
    }

    private List<String> parseDependencies(java.io.File pomFile) {
        List<String> dependencies = new ArrayList<>();
        if (pomFile.exists() && pomFile.isFile()) {
            try {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pomFile));
                dependencies = model.getDependencies().stream()
                        .map(dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion())
                        .collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }
        return dependencies;
    }

    private ProjectMetadata parseProjectMetadata(java.io.File pomFile) {
        ProjectMetadata projectMetadata = new ProjectMetadata();
        if (pomFile.exists() && pomFile.isFile()) {
            try {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pomFile));
                projectMetadata.setProjectName(model.getName());
                projectMetadata.setVersion(model.getVersion());
                List<String> developers = model.getDevelopers().stream()
                        .map(developer -> developer.getName()) // Fixed this line
                        .collect(Collectors.toList());
                projectMetadata.setDevelopers(developers);
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }else{
            // Default values if pom.xml does not exist
            projectMetadata.setProjectName("Unknown");
            projectMetadata.setVersion("Unknown");
            projectMetadata.setDevelopers(new ArrayList<>());
        }
        return projectMetadata;
    }

    private String parseJavaVersion(java.io.File pomFile) {
        if (pomFile.exists() && pomFile.isFile()) {
            try {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pomFile));
                String javaVersion = model.getProperties().getProperty("maven.compiler.source");

                if (javaVersion == null) {
                    // Try to get the version from the build section if it's not found in properties
                    Plugin compilerPlugin = model.getBuild().getPluginsAsMap().get("org.apache.maven.plugins:maven-compiler-plugin");
                    if (compilerPlugin != null) {
                        Xpp3Dom configuration = (Xpp3Dom) compilerPlugin.getConfiguration();
                        Xpp3Dom source = configuration.getChild("source");
                        if (source != null) {
                            javaVersion = source.getValue();
                        }
                    }
                }

                return javaVersion;
            } catch (Exception e) {
                System.err.println("Error reading Java version from pom.xml: " + e.getMessage());
                e.printStackTrace(); // Handle the exception appropriately
            }
        }
        return null; // Return null or a default value if the version is not found
    }

    private String extractProjectName(java.io.File pomFile) {
        if (pomFile.exists() && pomFile.isFile()) {
            try {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pomFile));
                return model.getName();
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }
        return "Unknown"; // Default value if the project name is not found
    }

    private String extractLanguage(java.io.File pomFile) {
        if (pomFile.exists() && pomFile.isFile()) {
            try {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pomFile));
                String javaVersion = model.getProperties().getProperty("maven.compiler.source");

                if (javaVersion == null) {
                    // Try to get the version from the build section if it's not found in properties
                    Plugin compilerPlugin = model.getBuild().getPluginsAsMap().get("org.apache.maven.plugins:maven-compiler-plugin");
                    if (compilerPlugin != null) {
                        Xpp3Dom configuration = (Xpp3Dom) compilerPlugin.getConfiguration();
                        Xpp3Dom source = configuration.getChild("source");
                        if (source != null) {
                            javaVersion = source.getValue();
                        }
                    }
                }

                return "Java " + javaVersion;
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }
        return "Unknown"; // Default value if the language is not found
    }

    private void calculateComplexityMetrics(Path projectPath, ParsedCode parsedCode) {
        ComplexityMetrics complexityMetrics = new ComplexityMetrics();
        int totalMethods = 0;
        int totalClasses = 0;

        for (com.codeglimmer.model.File file : parsedCode.getFiles()) {
            totalClasses += file.getClasses().size();
            for (Class clazz : file.getClasses()) {
                totalMethods += clazz.getMethods().size();
            }
        }

        complexityMetrics.setTotalMethods(totalMethods);
        complexityMetrics.setTotalClasses(totalClasses);
        parsedCode.setComplexityMetrics(complexityMetrics);
    }

    private ParserConfiguration.LanguageLevel parseLanguageLevel(String javaVersion) {
        switch (javaVersion) {
            case "1.8":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_8;
            case "9":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_9;
            case "10":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_10;
            case "11":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_11;
            case "12":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_12;
            case "13":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_13;
            case "14":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_14;
            case "15":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_15;
            case "16":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_16;
            case "17":
                return com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_17;
            default:
                return com.github.javaparser.ParserConfiguration.LanguageLevel.RAW;
        }
    }
}
