package com.interview.people.data;

import com.interview.people.OffsetBasedPageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PersonService {

    @Autowired
    private PersonRepository repository;


    public void test() {
        Person person = new Person();
        person.setFirstname("Marco");
        person.setLastname("Bianchi");
        person.setDescription("user");
        person.setEmail("marco@marcobianchi.it");
        person.setFiscalCode("CGHTYHJJ02B598H");
        person.setLastAccessDate(LocalDate.now());

        repository.save(person);

    }

    public List fetch(int offset, int limit) {
        Sort sort = Sort.by("lastname");
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

}
