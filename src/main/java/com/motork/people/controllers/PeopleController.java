package com.motork.people.controllers;

import com.motork.people.data.PersonService;
import com.motork.people.exceptions.InternalError;
import com.motork.people.exceptions.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.max.upload.size.mb:1.0}")
    private float maxUploadSizeMB;

    @Autowired
    private PersonService personService;

    @PostMapping("/submit")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("http submit request received");
        InputStream inputStream;

        // reject .txt extension in original filename
        String originalFilename=file.getOriginalFilename();
        if(!StringUtils.isEmpty(originalFilename)){
            String ext = FilenameUtils.getExtension(originalFilename);
            if(ext.equalsIgnoreCase("txt")){
                log.error(".txt file refused: "+originalFilename);
                throw new InvalidRequest(".txt extension is not accepted");
            }
        }

        // reject file exceeding max size
        float sizeMb=(float)file.getSize()/1024/1024;
        if(sizeMb>maxUploadSizeMB){
            log.error("file "+originalFilename+" rejected: too big ("+file.getSize()+" bytes)");
            throw new InvalidRequest("file too big: max size accepted is "+maxUploadSizeMB+"MB");
        }

        // create an InputStream from the MultipartFile
        try {
            inputStream = new BufferedInputStream(file.getInputStream());
        } catch (IOException e) {
            log.error("Error creating input stream from MultipartFile", e);
            throw new InternalError("Server error");
        }

        // invoke the service and manage the exceptions
        try {
            personService.importCsv(inputStream);
        } catch (IOException e) {
            throw new InternalError("Error processing input stream");
        } catch (InvalidMimeTypeException e) {
            throw new InvalidRequest("Not a csv file type");
        } catch (MissingEmailException e) {
            throw new InvalidRequest("Missing email in csv record");
        } catch (CsvParseException e) {
            throw new InternalError("Unparsable csv file");
        }

        // return a success response
        return new UploadFileResponse(file.getContentType(), file.getSize());

    }


    /**
     * Response model
     */
    static class UploadFileResponse {
        public String fileType;
        public long size;

        public UploadFileResponse(String fileType, long size) {
            this.fileType = fileType;
            this.size = size;
        }

    }


}
