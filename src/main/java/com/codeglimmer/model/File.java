package com.codeglimmer.model;

import java.util.ArrayList;
import java.util.List;

public class File {
    private String name;
    private List<Class> classes = new ArrayList<>();
    private List<Interface> interfaces = new ArrayList<>();
    private List<Enum> enums = new ArrayList<>();
    private List<Record> records = new ArrayList<>();
    private List<AnnotationClass> annotationClasses = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Class> getClasses() {
        return classes;
    }

    public void setClasses(List<Class> classes) {
        this.classes = classes;
    }

    public List<Interface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<Interface> interfaces) {
        this.interfaces = interfaces;
    }

    public List<Enum> getEnums() {
        return enums;
    }

    public void setEnums(List<Enum> enums) {
        this.enums = enums;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public List<AnnotationClass> getAnnotationClasses() {
        return annotationClasses;
    }

    public void setAnnotationClasses(List<AnnotationClass> annotationClasses) {
        this.annotationClasses = annotationClasses;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
