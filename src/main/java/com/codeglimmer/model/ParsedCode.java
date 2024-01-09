package com.codeglimmer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParsedCode {
    private String projectName;
    private String projectDescription;
    private String packageName;
    private String language;
    private List<String> dependencies = new ArrayList<>();
    private ProjectMetadata projectMetadata;
    private List<File> files = new ArrayList<>();
    private ComplexityMetrics complexityMetrics;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public ProjectMetadata getProjectMetadata() {
        return projectMetadata;
    }

    public void setProjectMetadata(ProjectMetadata projectMetadata) {
        this.projectMetadata = projectMetadata;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public ComplexityMetrics getComplexityMetrics() {
        return complexityMetrics;
    }

    public void setComplexityMetrics(ComplexityMetrics complexityMetrics) {
        this.complexityMetrics = complexityMetrics;
    }
}

