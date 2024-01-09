package com.codeglimmer.model;

import java.util.List;

public class AnnotationClass {
    private String name;
    private List<AnnotationElement> elements;
    private List<Annotation> annotations;
    private String description;

    // Getters and Setters


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AnnotationElement> getElements() {
        return elements;
    }

    public void setElements(List<AnnotationElement> elements) {
        this.elements = elements;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

