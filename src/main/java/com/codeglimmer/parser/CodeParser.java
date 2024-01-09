package com.codeglimmer.parser;

import com.codeglimmer.model.ParsedCode;

import java.io.IOException;
import java.nio.file.Path;

public interface CodeParser {
    ParsedCode parse(Path projectPath) throws IOException;

}
