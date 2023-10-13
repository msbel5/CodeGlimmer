package org.codeglimmer.parser;

import org.codeglimmer.model.ParsedCode;

public interface CodeParser {
    ParsedCode parse(String code);

}
