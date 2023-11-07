package com.crd.msreconciliation.services;

import com.crd.msreconciliation.utils.CustomCSVWriter;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static com.crd.msreconciliation.utils.StringConstants.*;

@Service
public class ReconciliationService {
    final Logger LOGGER = LoggerFactory.getLogger(ReconciliationService.class);
    public List<String> generateReconciliationReport(MultipartFile sourceFile, MultipartFile targetFile){
        final String ERROR_LOG = "Error Occurred :: ";
        File currentDir = new File(".");
        String outputFileName = currentDir.getPath()+"/reconciliation_report.csv";
        List<String> responseList = new LinkedList<>();
        try {
            List<String[]> sourceData = readCsvData(sourceFile);
            List<String[]> targetData = readCsvData(targetFile);

            List<String> sourceHeaders = Arrays.asList(sourceData.get(0));
            List<String> targetHeaders = Arrays.asList(targetData.get(0));


            FileWriter writer = new FileWriter(outputFileName);
            CSVWriter csvWriter = new CSVWriter(writer);

            CustomCSVWriter customCSVWriter = new CustomCSVWriter(writer);


            // Write headers
            customCSVWriter.writeNext(new String[]{HEADER_TYPE, HEADER_ID, HEADER_FIELD, HEADER_SOURCE, HEADER_TARGET});

            long missingInTarget = sourceData.stream()
                    .skip(1) // skip headers
                    .filter(sourceValues -> sourceValues.length >= targetHeaders.size())
                    .filter(sourceValues -> targetData.stream()
                            .skip(1) // skip headers
                            .filter(targetValues -> targetValues.length >= sourceHeaders.size())
                            .noneMatch(targetValues -> sourceValues[0].equals(targetValues[0]))
                    )
                    .peek(sourceValues -> customCSVWriter.writeNext(new String[]{TARGET_OUTPUT_MSG,null, sourceValues[0],null,null}))
                    .count();

            long missingInSource = targetData.stream()
                    .skip(1) // skip headers
                    .filter(targetValues -> targetValues.length >= sourceHeaders.size())
                    .filter(targetValues -> sourceData.stream()
                            .skip(1) // skip headers
                            .filter(sourceValues -> sourceValues.length >= targetHeaders.size())
                            .noneMatch(sourceValues -> targetValues[0].equals(sourceValues[0]))
                    )
                    .peek(targetValues -> customCSVWriter.writeNext(new String[]{SOURCE_OUTPUT_MSG, null, targetValues[0], null, null}))
                    .count();

            long fieldDiscrepancies = sourceData.stream()
                    .skip(1) // skip headers
                    .filter(sourceValues -> sourceValues.length >= targetHeaders.size())
                    .filter(sourceValues -> targetData.stream()
                            .skip(1) // skip headers
                            .filter(targetValues -> targetValues.length >= sourceHeaders.size())
                            .anyMatch(targetValues -> sourceValues[0].equals(targetValues[0]))
                    )
                    .filter(sourceValues -> targetData.stream()
                            .skip(1) // skip headers
                            .filter(targetValues -> targetValues.length >= sourceHeaders.size())
                            .anyMatch(targetValues -> sourceValues[0].equals(targetValues[0]))
                    )
                    .filter(sourceValues -> IntStream.range(1, sourceValues.length)
                            .anyMatch(i -> !sourceValues[i].equals(targetData.stream()
                                    .skip(1) // skip headers
                                    .filter(targetValues -> targetValues.length >= sourceHeaders.size())
                                    .filter(targetValues -> targetValues[0].equals(sourceValues[0]))
                                    .map(targetValues -> targetValues[i])
                                    .findFirst().orElse("")))
                    )
                    .peek(sourceValues -> {
                        for (int i = 1; i < sourceValues.length; i++) {
                            int index = i;
                            String sourceValue = sourceValues[i];
                            String targetValue = targetData.stream()
                                    .skip(1) // skip headers
                                    .filter(targetValues -> targetValues.length >= sourceHeaders.size())
                                    .filter(targetValues -> targetValues[0].equals(sourceValues[0]))
                                    .map(targetValues -> targetValues[index])
                                    .findFirst().orElse(null);
                            if (!sourceValue.equals(targetValue)) {
                                customCSVWriter.writeNext(new String[]{"Field Discrepancy", sourceValues[0], sourceHeaders.get(i), sourceValue, targetValue});
                            }
                        }
                    })
                    .count();

            csvWriter.close();

            responseList.add("Reconciliation completed:");
            responseList.add("- Records missing in target: " + missingInTarget);
            responseList.add("- Records missing in source: " + missingInSource);
            responseList.add("- Records with field discrepancies: " + fieldDiscrepancies);
            responseList.add("Report saved to: " + outputFileName);


        } catch (IOException e) {
            //Logger
            LOGGER.error(ERROR_LOG + e.getMessage());
            responseList.add(ERROR_LOG + e.getMessage());
        }
        return responseList;
    }
    //CSV file reader
    private List<String[]> readCsvData(MultipartFile file) throws IOException {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                data.add(values);
            }
        }
        return data;
    }

}
