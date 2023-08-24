package com.modcmga.backendservice.model.export;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

/**
 * Provides information for exporting files.
 */
@AllArgsConstructor
@Getter
public class ExportFile {
    /**
     * The file to be exported.
     */
    private File file;

    /**
     * Determines if the file has to be nested in a folder.
     */
    private boolean isNested;

    /**
     * The folder name where the file is nested. This is only used, when {@link #isNested} is true.
     */
    private String folder;
}
