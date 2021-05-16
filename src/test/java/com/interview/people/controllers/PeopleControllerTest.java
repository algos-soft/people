package com.interview.people.controllers;

import com.interview.people.data.Person;
import com.interview.people.data.PersonRepository;
import com.interview.people.data.PersonService;
import com.interview.people.exceptions.CsvParseException;
import com.interview.people.exceptions.InvalidMimeTypeException;
import com.interview.people.exceptions.MissingEmailException;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PeopleControllerTest {

    @InjectMocks
    private PersonService unit;

    @Mock
    private PersonRepository repository;

    @Mock
    private Tika mTika;

    @Mock
    private List<PersonService.ImportListener> importListeners;

    private HashMap<String, Person> mockStorage;

    @Before
    public void init() throws IOException {

        when(mTika.detect(any(InputStream.class))).thenReturn("text/plain");

        mockStorage=buildMockStorage();

        when(repository.findByEmail(anyString())).thenAnswer(args -> {
            return mockStorage.get(args.getArgument(0));
        });

        when(repository.save(any(Person.class))).thenAnswer(args -> {
            Person p = args.getArgument(0);
            mockStorage.put(p.getEmail(), p);
            return null;
        });

    }

    @Test
    // invalid content type must throw InvalidMimeTypeException
    public void testImportCsv1() {
        assertThrows(InvalidMimeTypeException.class, () -> {
            when(mTika.detect(any(InputStream.class))).thenReturn("application/pdf");
            unit.importCsv(buildStreamData());
        });
    }

    @Test
    // import a good csv on an empty storage, must add all records
    public void testImportCsv2() throws IOException, MissingEmailException, InvalidMimeTypeException, CsvParseException {
        mockStorage.clear();
        unit.importCsv(buildStreamData());
        assert(mockStorage.size()==3);
    }

    @Test
    // import a good csv, all persons are already present, expect same size
    public void testImportCsv3() throws IOException, MissingEmailException, InvalidMimeTypeException, CsvParseException {
        unit.importCsv(buildStreamData());
        assert(mockStorage.size()==3);
    }

    @Test
    // import good csv, 2 persons were not present, expect 2 more on db
    public void testImportCsv4() throws IOException, MissingEmailException, InvalidMimeTypeException, CsvParseException {
        int sizePre=mockStorage.size();
        unit.importCsv(buildStream2NewPersons());
        assert(mockStorage.size()==sizePre+2);
    }

    @Test
    // import invalid csv, expect CsvParseException
    public void testImportCsv5() {
        assertThrows(CsvParseException.class, () -> {
            unit.importCsv(buildStreamDInvalidCsv());
        });
    }

    @Test
    // import good csv but email missing, expect MissingEmailException
    public void testImportCsv6() {
        assertThrows(MissingEmailException.class, () -> {
            unit.importCsv(buildStreamMissingEmail());
        });
    }



    private InputStream buildStreamData() throws IOException {
        String streamData="madeleine.lewis@example.com,Lewis,Madeleine,KKGQI9BEGMR3RFS0,\"Dunedin, Taranaki\",07/03/2021\n";
        streamData+="terry.webb@example.com,Webb,Terry,Z20H0D8LDN43BARM,\"Erie, Hawaii\",09/11/2020\n";
        streamData+="emilie.mortensen@example.com,Mortensen,Emilie,MJYSYO8D9MZWMACH,\"Saltum, Hovedstaden\",20/02/2021";
        return IOUtils.toInputStream(streamData, "UTF-8");
    }


    private InputStream buildStream2NewPersons() throws IOException {
        String streamData="mason.johnson@example.com,Johnson,Mason,GPW69FWDUA3BQCQD,\"Cochrane, Newfoundland and Labrador\",16/01/2021\n";
        streamData+="jesus.fuentes@example.com,Fuentes,Jesus,JD0FTURRLZO2SJ60,\"Valencia, Navarra\",16/10/2020";
        return IOUtils.toInputStream(streamData, "UTF-8");
    }

    private InputStream buildStreamDInvalidCsv() throws IOException {
        String streamData="madeleine.lewis@example.com.Lewis,Madeleine;KKGQI9BEGMR3RFS0,\"Dunedin, Taranaki\",07/03/2021\n";
        streamData+=",Webb,Terry,Z20H0D8LDN43BARM,\"Erie, Hawaii\",09/11/2020\n";
        streamData+="emilie.mortensen@example.com,Mortensen,Emilie,MJYSYO8D9MZWMACH,\"Saltum, Hovedstaden\",20/02/2021";
        return IOUtils.toInputStream(streamData, "UTF-8");
    }


    private InputStream buildStreamMissingEmail() throws IOException {
        String streamData="madeleine.lewis@example.com,Lewis,Madeleine,KKGQI9BEGMR3RFS0,\"Dunedin, Taranaki\",07/03/2021\n";
        streamData+=",Webb,Terry,Z20H0D8LDN43BARM,\"Erie, Hawaii\",09/11/2020\n";
        streamData+="emilie.mortensen@example.com,Mortensen,Emilie,MJYSYO8D9MZWMACH,\"Saltum, Hovedstaden\",20/02/2021";
        return IOUtils.toInputStream(streamData, "UTF-8");
    }


    private HashMap<String,Person> buildMockStorage(){
        HashMap<String, Person> map = new HashMap<>();
        Person person;

        person = new Person();
        person.setId(1);
        person.setEmail("madeleine.lewis@example.com");
        person.setLastname("Lewis");
        person.setFirstname("Madeleine");
        person.setFiscalCode("KKGQI9BEGMR3RFS0");
        person.setDescription("Dunedin, Taranaki");
        person.setLastAccessDate(LocalDate.parse("07/03/2021", DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        map.put(person.getEmail(),person);

        person = new Person();
        person.setId(2);
        person.setEmail("terry.webb@example.com");
        person.setLastname("Webb");
        person.setFirstname("Terry");
        person.setFiscalCode("Z20H0D8LDN43BARM");
        person.setDescription("Erie, Hawaii");
        person.setLastAccessDate(LocalDate.parse("09/11/2020", DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        map.put(person.getEmail(),person);

        person = new Person();
        person.setId(3);
        person.setEmail("emilie.mortensen@example.com");
        person.setLastname("Mortensen");
        person.setFirstname("Terry");
        person.setFiscalCode("Z20H0D8LDN43BARM");
        person.setDescription("Erie, Hawaii");
        person.setLastAccessDate(LocalDate.parse("20/02/2021", DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        map.put(person.getEmail(),person);

        return map;

    }



}
