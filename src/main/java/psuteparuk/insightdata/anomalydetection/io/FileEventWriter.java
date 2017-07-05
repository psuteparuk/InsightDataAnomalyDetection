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

public class FileEventWriter {
    final private String outputFilePath;

    public FileEventWriter(String outputFilePath) {
        this.outputFilePath = outputFilePath;
        this.clearFile();
    }

    public void write(String message) {
        try {
            Files.write(
                Paths.get(this.outputFilePath),
                Collections.singletonList(message),
                UTF_8,
                APPEND,
                CREATE
            );
        } catch (IOException e) {
            System.out.println("Cannot write to file. Please make sure the output folder exists.");
            System.exit(1);
        }
    }

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
