package com.codeglimmer.services;

import com.codeglimmer.model.*;
import com.codeglimmer.model.Class;
import com.codeglimmer.model.Enum;
import com.codeglimmer.model.Record;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class DescriptionService {

    private static final String API_KEY = "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";
    private static final String ENGINE_ID = "text-davinci-003"; // You can choose other engines like "davinci", "curie", etc.
    private static final String API_URL = "https://api.openai.com/v1/engines/" + ENGINE_ID + "/completions";

    public String generateDescription(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + API_KEY);
        con.setDoOutput(true);

        JSONObject data = new JSONObject();
        data.put("prompt", prompt);
        data.put("temperature", 0.7);
        data.put("max_tokens", 150);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = data.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("Failed to generate description: HTTP code " + code);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return new JSONObject(response.toString()).getJSONArray("choices").getJSONObject(0).getString("text").trim();
        }
    }

    public String generateMethodDescription(Method method) throws Exception {
        String methodName = method.getName();
        String parameters = String.join(", ", method.getParameters());
        String returnType = method.getReturnType();
        String code = method.getCode();
        String annotations = method.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "I have a Java method named '%s'. It takes parameters (%s) and returns a '%s' type. " +
                        "Here is the code snippet: \n```\n%s\n```\n" +
                        "It has the following annotations: %s. " +
                        "Can you provide a detailed and concise description of this method, including its purpose, functionality, and usage?",
                methodName, parameters, returnType, code, annotations.isEmpty() ? "none" : annotations
        );

        return generateDescription(prompt);
    }

    public String generateFieldDescription(Field field) throws Exception {
        String name = field.getName();
        String type = field.getType();
        String defaultValue = field.getDefaultValue();
        String annotations = field.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.joining(", "));

        // Assuming you have a way to get the field code as a string
        String fieldCode = getFieldCodeAsString(field);

        String prompt = String.format(
                "I have a Java field named '%s' of type '%s' with default value '%s' and annotations (%s). " +
                        "Here is the field code: \n```\n%s\n```\n" +
                        "Can you provide a detailed and concise description of this field, including its purpose and usage?",
                name, type, defaultValue == null ? "none" : defaultValue, annotations.isEmpty() ? "none" : annotations, fieldCode
        );

        return generateDescription(prompt);
    }

    public String generateAnnotationDescription(Annotation annotation) throws Exception {
        String name = annotation.getName();
        String parameters = annotation.getParameters().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "I have a Java annotation named '%s' with parameters (%s). " +
                        "Can you provide a detailed and concise description of this annotation, including its purpose and usage?",
                name, parameters.isEmpty() ? "none" : parameters
        );

        return generateDescription(prompt);
    }


    public String generateClassDescription(Class clazz) throws Exception {
        String name = clazz.getName();
        String methods = clazz.getMethods().stream()
                .map(Method::getName)
                .collect(Collectors.joining(", "));
        String fields = clazz.getFields().stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "));
        String annotations = clazz.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.joining(", "));

        // Assuming you have a way to get the entire class code as a string
        String classCode = getClassCodeAsString(clazz);

        String prompt = String.format(
                "I have a Java class named '%s' with methods (%s), fields (%s), and annotations (%s). " +
                        "Here is the entire class code: \n```\n%s\n```\n" +
                        "Can you provide a detailed and concise description of this class, including its purpose, functionality, and usage?",
                name, methods, fields, annotations.isEmpty() ? "none" : annotations, classCode
        );

        return generateDescription(prompt);
    }


    public String generateEnumDescription(com.codeglimmer.model.Enum enumModel) throws Exception {
        String name = enumModel.getName();
        String values = String.join(", ", enumModel.getValues());
        String annotations = enumModel.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "I have a Java enum named '%s' with values (%s) and annotations (%s). " +
                        "Can you provide a detailed and concise description of this enum, including its purpose and usage?",
                name, values, annotations.isEmpty() ? "none" : annotations
        );

        return generateDescription(prompt);
    }

    public String generateInterfaceDescription(Interface interfaceModel) throws Exception {
        String name = interfaceModel.getName();
        String methods = interfaceModel.getMethods().stream()
                .map(Method::getName)
                .collect(Collectors.joining(", "));
        String annotations = interfaceModel.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "I have a Java interface named '%s' with methods (%s) and annotations (%s). " +
                        "Can you provide a detailed and concise description of this interface, including its purpose and usage?",
                name, methods, annotations.isEmpty() ? "none" : annotations
        );

        return generateDescription(prompt);
    }

    public String generateProjectDescription(ParsedCode parsedCode) throws Exception {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Provide a comprehensive overview of a Java project named '")
                .append(parsedCode.getProjectName())
                .append("'. The project is written in '")
                .append(parsedCode.getLanguage())
                .append("' and depends on the following libraries or frameworks: '")
                .append(String.join(", ", parsedCode.getDependencies()))
                .append("'.\n\n");

        // If you have more specific information about the project's purpose or key functionalities, include them here
        prompt.append("The project includes various classes and interfaces that collaborate to offer the following key functionalities:\n");

        for (File file : parsedCode.getFiles()) {
            for (Class clazz : file.getClasses()) {
                prompt.append("- Class '").append(clazz.getName()).append("': ")
                        .append(clazz.getDescription()).append("\n");
            }

            for (Interface interfaceModel : file.getInterfaces()) {
                prompt.append("- Interface '").append(interfaceModel.getName()).append("': ")
                        .append(interfaceModel.getDescription()).append("\n");
            }

            for (com.codeglimmer.model.Enum enumModel : file.getEnums()) {
                prompt.append("- Enum '").append(enumModel.getName()).append("': ")
                        .append(enumModel.getDescription()).append("\n");
            }
        }

        prompt.append("\nProvide insights into how these components interact, the flow of data, and how users or developers can utilize this project effectively.");

        return generateDescription(prompt.toString());

    }


    public String getClassCodeAsString(Class clazz) {
        StringBuilder classCode = new StringBuilder();

        // Adding annotations
        for (Annotation annotation : clazz.getAnnotations()) {
            classCode.append("@").append(annotation.getName());
            if (!annotation.getParameters().isEmpty()) {
                classCode.append("(")
                        .append(annotation.getParameters().entrySet().stream()
                                .map(entry -> entry.getKey() + " = " + entry.getValue())
                                .collect(Collectors.joining(", ")))
                        .append(")");
            }
            classCode.append("\n");
        }

        // Adding class declaration
        classCode.append("public class ").append(clazz.getName()).append(" {\n");

        // Adding fields
        for (Field field : clazz.getFields()) {
            classCode.append("\t").append(field.getType()).append(" ").append(field.getName()).append(";\n");
        }

        // Adding methods
        for (Method method : clazz.getMethods()) {
            classCode.append("\tpublic ").append(method.getReturnType())
                    .append(" ").append(method.getName()).append("(")
                    .append(String.join(", ", method.getParameters())).append(") {\n")
                    .append("\t\t").append(method.getCode()).append("\n")
                    .append("\t}\n");
        }

        classCode.append("}\n");

        return classCode.toString();
    }

    public String generateAnnotationClassDescription(AnnotationClass annotationClass) throws Exception {
        String name = annotationClass.getName();
        String elements = annotationClass.getElements().stream()
                .map(AnnotationElement::getName)
                .collect(Collectors.joining(", "));
        String annotations = annotationClass.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "I have a Java annotation class named '%s' with elements (%s) and annotations (%s). " +
                        "Can you provide a detailed and concise description of this annotation class, including its purpose and usage?",
                name, elements.isEmpty() ? "none" : elements, annotations.isEmpty() ? "none" : annotations
        );

        return generateDescription(prompt);
    }





    public String getFieldCodeAsString(Field field) {
        StringBuilder fieldCode = new StringBuilder();

        // Adding annotations
        for (Annotation annotation : field.getAnnotations()) {
            fieldCode.append("@").append(annotation.getName());
            if (!annotation.getParameters().isEmpty()) {
                fieldCode.append("(")
                        .append(annotation.getParameters().entrySet().stream()
                                .map(entry -> entry.getKey() + " = " + entry.getValue())
                                .collect(Collectors.joining(", ")))
                        .append(")");
            }
            fieldCode.append("\n");
        }

        // Adding field declaration
        fieldCode.append(field.getType()).append(" ").append(field.getName());

        // Adding default value if exists
        if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
            fieldCode.append(" = ").append(field.getDefaultValue());
        }

        fieldCode.append(";");

        return fieldCode.toString();
    }

    public String generateRecordDescription(Record record) throws Exception {
        String name = record.getName();
        String components = String.join(", ", record.getComponents());
        String annotations = record.getAnnotations().stream()
                .map(Annotation::getName)
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "I have a Java record named '%s' with components (%s) and annotations (%s). " +
                        "Can you provide a detailed and concise description of this record, including its purpose, structure, and usage?",
                name, components, annotations.isEmpty() ? "none" : annotations
        );

        return generateDescription(prompt);
    }



    public void fillDescriptions(ParsedCode parsedCode) throws Exception {

        DescriptionService descriptionService = new DescriptionService();

        for (com.codeglimmer.model.File file : parsedCode.getFiles()) {
            for (Class clazz : file.getClasses()) {
                clazz.setDescription(descriptionService.generateClassDescription(clazz));

                for (Method method : clazz.getMethods()) {
                    method.setDescription(descriptionService.generateMethodDescription(method));
                }

                for (Field field : clazz.getFields()) {
                    field.setDescription(descriptionService.generateFieldDescription(field));
                }

                for (Annotation annotation : clazz.getAnnotations()) {
                    annotation.setDescription(descriptionService.generateAnnotationDescription(annotation));
                }
            }

            for (Interface interfaceModel : file.getInterfaces()) {
                interfaceModel.setDescription(descriptionService.generateInterfaceDescription(interfaceModel));
            }

            for (Enum enumModel : file.getEnums()) {
                enumModel.setDescription(descriptionService.generateEnumDescription(enumModel));
            }

            for (Record record : file.getRecords()) {
                // Assuming there's a similar method for Record in your DescriptionService
                record.setDescription(descriptionService.generateRecordDescription(record));
            }

            for (AnnotationClass annotationClass : file.getAnnotationClasses()) {
                annotationClass.setDescription(descriptionService.generateAnnotationClassDescription(annotationClass));
            }
        }

        parsedCode.setProjectDescription(descriptionService.generateProjectDescription(parsedCode));

    }


}

