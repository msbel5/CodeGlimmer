package com.codeglimmer.writer;

import com.codeglimmer.model.ParsedCode;

public interface CodeWriter {
    byte[] write(ParsedCode parsedCode);
}
