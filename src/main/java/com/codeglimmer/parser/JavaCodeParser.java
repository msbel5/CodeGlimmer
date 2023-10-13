package org.codeglimmer.parser;

import org.codeglimmer.model.ParsedCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaCodeParser implements CodeParser {
    private static final Logger logger = LoggerFactory.getLogger(JavaCodeParser.class);

    @Override
    public ParsedCode parse(String code) {
        logger.info("Parsing Java code");

        ParsedCode parsedCode = new ParsedCode();
        //parsedCode.setClasses(extractClasses(code));
        //parsedCode.setMethods(extractMethods(code));
        // TODO: Implement extraction of comments and other elements

        logger.info("Parsing completed");
        return parsedCode;
    }

    private List<String> extractClasses(String code) {
        List<String> classes = new ArrayList<>();
        Pattern pattern = Pattern.compile("class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            classes.add(matcher.group(1));
        }

        return classes;
    }

    private List<String> extractMethods(String code) {
        List<String> methods = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            methods.add(matcher.group(2));
        }

        return methods;
    }
}
