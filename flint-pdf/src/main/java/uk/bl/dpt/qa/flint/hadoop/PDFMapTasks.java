package uk.bl.dpt.qa.flint.hadoop;

import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.Flint;
import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.pdf.converter.PDFToText;
import uk.bl.dpt.qa.flint.wrappers.Tools;
import uk.bl.dpt.utils.checksum.ChecksumUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.hadoop.fs.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PDFMapTasks implements AdditionalMapTasks {

    private static Logger LOGGER = LoggerFactory.getLogger(PDFMapTasks.class);

    private static final boolean shouldExtractText = true;
    private static final boolean shouldZipFlint = true;

    private static boolean textExtractSuccess = false;

    @Override
    public void map(FileSystem hdFS, File localTempDir, Path outputDir, File localFile, List<CheckResult> results) {
        Map<String, String> checksums = new HashMap<String, String>();
        List<String> generatedFiles = new LinkedList<String>();

        if (shouldExtractText) extractText(localFile, generatedFiles, checksums);

        if (shouldZipFlint) zipFlint(hdFS, localTempDir, localFile, outputDir, generatedFiles, checksums, results);

    }

    /**
     * Extracts text from a PDF and links it to its generated checksum.
     *
     * @param localFile the file to extract text from
     * @param generatedFiles the list of generated files to which the extracted text-file belongs
     * @param checksums the Map linking the checksums to the extracted text
     */
    public void extractText(File localFile, List<String> generatedFiles, Map<String, String> checksums) {
        File fileTXT = new File(localFile.getAbsolutePath() + ".txt");
        textExtractSuccess = PDFToText.process(localFile, fileTXT);
        generatedFiles.add(fileTXT.getName());
        try {
            checksums.put(fileTXT.getName(), ChecksumUtil.generateChecksum(fileTXT.getAbsolutePath()));
        } catch (IOException e) {
            LOGGER.error("Caught IOException while trying to extract text: {}", e);
        }
        LOGGER.debug("txt: {}, size: {}", fileTXT.getAbsolutePath(), fileTXT.length());
    }

    /**
     * Prints the check-results as xml to a file, which is zipped, linked to its checksum
     * and saved to HDFS.
     *
     * @param hdFS the hadoop filesystem
     * @param localTempDir the temporary directory on the local node
     * @param outputDir the directory to write to on HDFS
     * @param localFile the file of concern that just got checked
     * @param generatedFiles the list of generated files to which the zipped xml-file belongs
     * @param checksums the Map linking the checksums to the zipfile
     * @param results a list of check results
     */
    public void zipFlint(FileSystem hdFS, File localTempDir, File localFile, Path outputDir,
                         List<String> generatedFiles, Map<String, String> checksums,
                         List<CheckResult> results) {
        File fileXML = new File(localFile.getAbsolutePath() + ".report.xml");
        File fileZIP = new File(localFile.getAbsolutePath() + ".zip");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(fileXML));
            Flint.printResults(results, pw);
            generatedFiles.add(fileXML.getName());
            checksums.put(fileXML.getName(), ChecksumUtil.generateChecksum(fileXML.getAbsolutePath()));
            LOGGER.debug("xml: {}, size: {}", fileXML.getAbsolutePath(), fileXML.length());
            Tools.zipGeneratedFiles(textExtractSuccess, checksums, generatedFiles, fileZIP.getAbsolutePath(), localTempDir.getAbsolutePath() + "/");
        } catch (IOException e) {
            LOGGER.error("Caught IOException while trying to zip xml to checksums: {}", e);
        } finally {
            if (pw != null) pw.close();
            if (localFile.exists()) localFile.delete();
            if (fileXML.exists()) fileXML.delete();
            if (fileZIP.exists()) {
                // store zip file
                try {
                    hdFS.copyFromLocalFile(false, false, new Path(fileZIP.getAbsolutePath()), outputDir);
                } catch (IOException e) {
                    LOGGER.error("Caught IOException while trying to copy zip-file to HDFS: {}", e);
                }
                fileZIP.delete();
            }
        }
    }
}
