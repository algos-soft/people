package com.interview.people.data;

import com.interview.people.tools.OffsetBasedPageRequest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class PersonService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // supported date patterns
    private static final String[] PATTERNS=new String[]{"yyyyMMdd","dd/MM/yyyy","yyyy/MM/dd","yy/MM/dd","dd/MM/yy","dd-MM-yyyy","yyyy-MM-dd","yy-MM-dd","dd-MM-yy","dd.MM.yyyy","yyyy.MM.dd","yy.MM.dd","dd.MM.yy"};

    @Autowired
    private PersonRepository repository;

    private Tika tika;

    @PostConstruct
    private void init(){
        tika=new Tika();
    }


    public List fetch(int offset, int limit) {
//        Sort sort = Sort.by("email");
        Sort sort=Sort.unsorted();
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, sort);
        Page<Person> page = repository.findAll(pageable);
        return page.toList();
    }


    public int count() {
        return (int)repository.count();
    }


    public void deleteAll() {
        repository.deleteAll();
    }


    public void importCsv(InputStream inputStream) throws Exception {

        // check text/plain MIME type
        String mimeType=tika.detect(inputStream);
        if(!mimeType.equals("text/plain")){
            Exception e = new Exception("Content type is not text/plain");
            log.error("could not import CSV", e);
            throw e;
        }

        Reader targetReader = new InputStreamReader(inputStream);
        CSVParser csvParser = new CSVParser(targetReader, CSVFormat.DEFAULT);

        Person person;
        try {
            for(CSVRecord csvRecord : csvParser){
                person=new Person();
                person.setEmail(csvRecord.get(0));
                person.setLastname(csvRecord.get(1));
                person.setFirstname(csvRecord.get(2));
                person.setFiscalCode(csvRecord.get(3));
                person.setDescription(csvRecord.get(4));
                person.setLastAccessDate(parseDate(csvRecord.get(5)));

                processPerson(person);

            }
        }catch (Exception e){
            // catch to log and rethrow
            log.error("Error parsing csv format",e);
            throw e;
        }

    }



    private void processPerson(Person csvPerson) throws Exception{

        // email is mandatory
        if(StringUtils.isEmpty(csvPerson.getEmail())){
            Exception e = new Exception("Missing email address for: "+csvPerson);
            log.error("Could not process entry",e);
            throw e;
        }

        // find a entity with this email. If not found, create it
        String action="updated";
        Person person = repository.findByEmail(csvPerson.getEmail());
        if(person==null){   // not present, create a new entity
            person=new Person();
            person.setEmail(csvPerson.getEmail());
            action="added";
        }

        // update and save
        updatePerson(person, csvPerson);
        repository.save(person);

        log.info("Person "+action+": "+person);

    }


    private void updatePerson(Person person,Person csvPerson){
        person.setLastname(csvPerson.getLastname());
        person.setFirstname(csvPerson.getFirstname());
        person.setFiscalCode(csvPerson.getFiscalCode());
        person.setDescription(csvPerson.getDescription());
        person.setLastAccessDate(csvPerson.getLastAccessDate());
    }


    private LocalDate parseDate(String dateString) throws ParseException {
        if(StringUtils.isEmpty(dateString)){
            return null;
        }
        Date date = DateUtils.parseDate(dateString, PATTERNS);
        return new java.sql.Date(date.getTime()).toLocalDate();
    }


}
