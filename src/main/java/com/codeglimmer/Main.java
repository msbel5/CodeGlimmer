package com.codeglimmer;

import com.codeglimmer.model.ParsedCode;
import com.codeglimmer.parser.CodeParser;
import com.codeglimmer.parser.JavaCodeParser;
import com.codeglimmer.services.DescriptionService;
import com.codeglimmer.writer.CodeWriter;
import com.codeglimmer.writer.JsonWriter;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        logger.info("CodeGlimmer Application Started");

        // Cloning a Git repository
        String repoUrl = "https://github.com/msbel5/seleniumExamplev2.git";
        logger.info("Cloning the repository: {}", repoUrl);
        File repoDir = new File("clonedRepo");  // Directory to clone the repo into
        // Check if the directory to clone into already exists and delete it if it does
        if(repoDir.exists()) {
            logger.info("'{}' already exists and will be deleted", repoDir.getName());
            FileUtils.deleteDirectory(repoDir);
        }
        try {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(repoDir)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("msbel5@gmail.com", "*****"))
                    .call();
        } catch (GitAPIException e) {
            logger.error("Error cloning the repository: {}", e.getMessage());
            return;
        }

        try {
            // Parsing the Java code
            CodeParser codeParser = new JavaCodeParser();
            ParsedCode parsedCode = codeParser.parse(Paths.get(repoDir.getPath()));
            DescriptionService descriptionService = new DescriptionService();
            parsedCode = descriptionService.fillDescriptions(parsedCode);

            // Check if parsedCode is not null before proceeding
            if (parsedCode != null) {
                // Writing the parsed code information into JSON format
                CodeWriter codeWriter = new JsonWriter();
                String jsonOutput = new String(codeWriter.write(parsedCode), StandardCharsets.UTF_8);

                // Printing the JSON output
                logger.info("JSON Output: {}", jsonOutput);

                // Writing the JSON output to a file
                Files.write(Paths.get("output.json"), jsonOutput.getBytes(StandardCharsets.UTF_8));
                logger.info("JSON output written to output.json");
                repoDir.delete();
            } else {
                logger.error("Parsed code is null. Please check the code parser.");
            }
        } catch (IOException e) {
            logger.error("Error: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage());
        }

        logger.info("CodeGlimmer Application Ended");

    }

}

