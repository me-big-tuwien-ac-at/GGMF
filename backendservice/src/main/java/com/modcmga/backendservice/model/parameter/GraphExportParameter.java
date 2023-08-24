package com.modcmga.backendservice.model.parameter;
/**
 * @Package: com.modcmga.poc.model
 * @Class: GraphMLExportType
 * @Author: Jan
 * @Date: 03.11.2021
 */

import lombok.Getter;
import lombok.Setter;

/**
 * This class defines the parameter which are related to the export behaviour
 * of the graph.
 */
@Getter
@Setter
public class GraphExportParameter {
    /**
     * The file name without the file extension.
     */
    private String fileName;

    /**
     * Defines in the folder name where nested files are stored.
     */
    private String folderName;

    @Override
    public String toString() {
        return String.format("folderName; %s",
                this.folderName);
    }
}
