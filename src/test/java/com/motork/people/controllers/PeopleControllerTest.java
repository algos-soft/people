package com.motork.people.controllers;

import com.motork.people.data.PersonService;
import com.motork.people.exceptions.InternalError;
import com.motork.people.exceptions.*;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class PeopleControllerTest {

    @InjectMocks
    private PeopleController unit;

    @Mock
    private PersonService personService;

    @Before
    public void init() {
        unit.setMaxUploadSizeMB(2.0f);
    }

    @Test
    // .txt file must throw InvalidRequest exception
    public void testUploadFile1() throws IOException {
        MultipartFile mpartFile = new MockMultipartFile("persons.txt", "persons.txt", "text/plain", buildStreamData());
        assertThrows(InvalidRequest.class, () -> {
            unit.uploadFile(mpartFile);
        });
    }

    @Test
    // test valid completion
    public void testUploadFile2() throws IOException {
        MultipartFile mpartFile = new MockMultipartFile("persons.csv", "persons.csv", "text/plain", buildStreamData());
        PeopleController.UploadFileResponse resp = unit.uploadFile(mpartFile);
        assert (resp != null);
        assert (resp.fileType.equals("text/plain"));
    }

    @Test
    // test throw InvalidRequest on MissingEmailException
    public void testUploadFile3() throws IOException, MissingEmailException, InvalidMimeTypeException, CsvParseException {
        MultipartFile mpartFile = new MockMultipartFile("persons.csv", "persons.csv", "text/plain", buildStreamData());
        doThrow(MissingEmailException.class).when(personService).importCsv(any(InputStream.class));
        assertThrows(InvalidRequest.class, () -> {
            unit.uploadFile(mpartFile);
        });
    }

    @Test
    // test throw InvalidRequest on InvalidMimeTypeException
    public void testUploadFile4() throws IOException, MissingEmailException, InvalidMimeTypeException, CsvParseException {
        MultipartFile mpartFile = new MockMultipartFile("persons.csv", "persons.csv", "text/plain", buildStreamData());
        doThrow(InvalidMimeTypeException.class).when(personService).importCsv(any(InputStream.class));
        assertThrows(InvalidRequest.class, () -> {
            unit.uploadFile(mpartFile);
        });
    }

    @Test
    // test throw InternalError on CsvParseException
    public void testUploadFile5() throws IOException, MissingEmailException, InvalidMimeTypeException, CsvParseException {
        MultipartFile mpartFile = new MockMultipartFile("persons.csv", "persons.csv", "text/plain", buildStreamData());
        doThrow(CsvParseException.class).when(personService).importCsv(any(InputStream.class));
        assertThrows(InternalError.class, () -> {
            unit.uploadFile(mpartFile);
        });
    }

    @Test
    // test throw InternalError on IOException
    public void testUploadFile6() throws IOException, MissingEmailException, InvalidMimeTypeException, CsvParseException {
        MultipartFile mpartFile = new MockMultipartFile("persons.csv", "persons.csv", "text/plain", buildStreamData());
        doThrow(IOException.class).when(personService).importCsv(any(InputStream.class));
        assertThrows(InternalError.class, () -> {
            unit.uploadFile(mpartFile);
        });
    }


    private InputStream buildStreamData() throws IOException {
        String streamData = "madeleine.lewis@example.com,Lewis,Madeleine,KKGQI9BEGMR3RFS0,\"Dunedin, Taranaki\",07/03/2021\n";
        streamData += "terry.webb@example.com,Webb,Terry,Z20H0D8LDN43BARM,\"Erie, Hawaii\",09/11/2020\n";
        streamData += "emilie.mortensen@example.com,Mortensen,Emilie,MJYSYO8D9MZWMACH,\"Saltum, Hovedstaden\",20/02/2021";
        return IOUtils.toInputStream(streamData, "UTF-8");
    }

}
