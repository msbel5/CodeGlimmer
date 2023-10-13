package org.codeglimmer.model;

import java.util.List;

public class ParsedCode {
    private List<String> classes;
    private List<String> methods;
    private List<String> comments;

    // Getters and setters

    @Override
    public String toString() {
        return "ParsedCode{" +
                "classes=" + classes +
                ", methods=" + methods +
                ", comments=" + comments +
                '}';
    }

}
