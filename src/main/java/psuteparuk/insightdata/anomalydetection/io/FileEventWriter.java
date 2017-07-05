package psuteparuk.insightdata.anomalydetection.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * A file writer engine that appends new content to the specified output file.
 * It clears the previous content in the constructor.
 */
public class FileEventWriter {
    final private String outputFilePath;

    /**
     * Assign output file and clear the content right away.
     * @param outputFilePath
     */
    public FileEventWriter(String outputFilePath) {
        this.outputFilePath = outputFilePath;
        this.clearFile();
    }

    /**
     * Opens an io connection and append {@message} to the output file.
     * @param message
     */
    public void write(String message) {
        try {
            Files.write(
                Paths.get(this.outputFilePath),
                Collections.singletonList(message),
                UTF_8,
                APPEND, // Append new content
                CREATE // If the output file is not found, create a new one.
            );
        } catch (IOException e) {
            System.out.println("Cannot write to file. Please make sure the output folder exists.");
            System.exit(1);
        }
    }

    /**
     * Clear the content of the output file.
     */
    private void clearFile() {
        try {
            FileWriter fileWriter = new FileWriter(this.outputFilePath, false);
            PrintWriter printWriter = new PrintWriter(fileWriter, false);
            printWriter.flush();
            printWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Cannot clear the output file. (File may not exist.)");
        }
    }
}
