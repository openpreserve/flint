package uk.bl.dpt.qa.flint.hadoop;

/**
 * Interface to be implemented by a {@link uk.bl.dpt.qa.flint.formats.Format} implementation
 * in case additional hadoop functionality wants to be implemented via
 * {@link uk.bl.dpt.qa.flint.hadoop.AdditionalMapTasks}
 */
public interface HadoopFormat {
    /**
     * Get tasks in addition to the standard behaviour of a FlintHadoop mapper.
     * @return {@link uk.bl.dpt.qa.flint.hadoop.AdditionalMapTasks}
     */
    public AdditionalMapTasks getAdditionalMapTasks();
}
