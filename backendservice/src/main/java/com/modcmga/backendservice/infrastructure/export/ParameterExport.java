package com.modcmga.backendservice.infrastructure.export;
/**
 * @Package: com.modcmga.backendservice.infrastructure.export
 * @Class: ParameterExport
 * @Author: Jan
 * @Date: 06.04.2022
 */

import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Creates a CSV-file with all parameters used for the complete process.
 */
@Component
public class ParameterExport {

    private final static String APPLICATION_PARAMETER_FILE_NAME =
            "applicationparameter.csv";

    public File createParameterFile(ModularisationParameter modularisationParameter) {
        var file = new File(APPLICATION_PARAMETER_FILE_NAME);

        try (var fileWriter = new FileWriter(file);
             var bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(modularisationParameter.toString());

        } catch (IOException e) {
            // TODO: handle write error
        }

        return file;
    }
}
