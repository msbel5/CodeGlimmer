# CodeGlimmer: AI-Powered Code Description Generator

## Description

CodeGlimmer is a Java-based application designed to automatically generate concise and detailed descriptions for various code elements such as classes, methods, fields, and annotations. Utilizing advanced AI models from OpenAI and Hugging Face, CodeGlimmer aims to enhance code understanding and documentation efficiency.

The application parses Java code, extracts key elements, and then uses AI models to generate human-like, understandable descriptions. This tool is particularly useful for developers looking to automate documentation or gain quick insights into complex codebases.

## Features

- **AI-Driven Descriptions**: Leverages models like GPT-3 and CodeBERT for generating descriptions.
- **Support for Multiple Code Elements**: Works with classes, methods, fields, enums, interfaces, and more.
- **Customizable Description Templates**: Tailor the output to suit various documentation styles.
- **Integration with Java Projects**: Easily plugs into existing Java codebases for analysis.

## Installation

Clone the repository and set up the project in your Java IDE:

```bash
git clone https://github.com/yourusername/CodeGlimmer.git
cd CodeGlimmer
# Follow your IDE's process to import and set up the project
```

## Usage

After setting up the project, you can use CodeGlimmer to generate descriptions for your Java code. Here's a basic example:

```java
// Example of generating a description for a Java class
Class myClass = ... // your class object
String description = descriptionService.generateClassDescription(myClass);
System.out.println(description);
```

## Configuration

Before using CodeGlimmer, ensure you have API keys for OpenAI and Hugging Face, and configure them in the DescriptionService.

## Acknowledgments

Special thanks to everyone who contributed to the development and enhancement of CodeGlimmer.

OpenAI and Hugging Face for their powerful AI models.


