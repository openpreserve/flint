package uk.bl.dpt.qa.flint.hadoop;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import uk.bl.dpt.qa.flint.checks.CheckResult;

import java.io.File;
import java.util.List;

/**
 * Interface to be implemented in case additional functionality wants to be achieved in addition
 * to the default FlintHadoop MapReduce behaviour.
 *
 */
public interface AdditionalMapTasks {

    /**
     * After a file is checked in the default map process of FlintHadoop, it asks the file-specific
     * Format for additional map tasks. If a Format implementation defines such AdditionalMapTasks,
     * each will be called with the following parameters that allow to copy/move files between the
     * local node and HDFS as well as modifying the results produced by the default map task.
     *
     * @param hdFS the hadoop filesystem
     * @param localTempDir the temporary directory on the local node
     * @param outputDir the directory to write to on HDFS
     * @param localFile the file of concern that just got checked
     * @param results a list of check results
     */
    public void map (FileSystem hdFS, File localTempDir, Path outputDir, File localFile,
                     List<CheckResult> results);
}
