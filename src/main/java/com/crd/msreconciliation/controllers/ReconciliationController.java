package com.crd.msreconciliation.controllers;

import com.crd.msreconciliation.services.ReconciliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/data-tool") //exposed endpoint to front end
public class ReconciliationController {
    final Logger LOGGER = LoggerFactory.getLogger(ReconciliationController.class); //initialize class logger
    private ReconciliationService reconciliationService;

    //constructor
    @Autowired
    public ReconciliationController(ReconciliationService reconciliationService){
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/reconciliation")
    @CrossOrigin(origins = "*")
    /*
    Receive the two files from front end and process
     */
    public ResponseEntity<List<String>> reconcileCSVFiles(@RequestParam("sourceFile") MultipartFile sourceFile,
                                                                        @RequestParam("targetFile") MultipartFile targetFile) {
        LOGGER.info("Reconciliation request received");
        List<String> reconciliationReport = reconciliationService.generateReconciliationReport(sourceFile,targetFile);

        if(reconciliationReport.size()>0){
            LOGGER.info("Reconciliation response");
            return ResponseEntity.ok(reconciliationReport); //return a list on successful recon
        }
        else{
            LOGGER.info("An error occurred");
            return (ResponseEntity<List<String>>) ResponseEntity.status(500); //return in case of an error
        }
    }
}
