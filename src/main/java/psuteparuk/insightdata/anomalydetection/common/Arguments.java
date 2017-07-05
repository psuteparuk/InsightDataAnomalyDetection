package psuteparuk.insightdata.anomalydetection.common;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Handle CLI arguments using JCommander
 * There are three required arguments
 *  --batch, --stream, and --flagged
 * that specify the input and output files.
 */
public class Arguments {
    @Parameter(
        names = { "--batch" },
        description = "Batch Log file path (required)",
        required = true)
    public String batchFilePath = null;

    @Parameter(
        names = { "--stream" },
        description = "Stream Log file path (required)",
        required = true)
    public String streamFilePath = null;

    @Parameter(
        names = { "--flagged" },
        description = "Flagged Purchases file path (required)",
        required = true)
    public String flaggedFilePath = null;

    @Parameter(
        names = { "-h", "--help" },
        description = "Print this usage",
        help = true)
    public boolean help;

    public Arguments(String[] args) {
        this.parseArguments(args);
    }

    private void parseArguments(String[] args) {
        JCommander jc = JCommander.newBuilder()
            .addObject(this)
            .build();

        try {
            jc.parse(args);

            if (this.help) {
                jc.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            jc.usage();
            System.exit(0);
        }

    }
}
