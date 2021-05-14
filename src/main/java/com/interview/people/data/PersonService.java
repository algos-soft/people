package com.interview.people.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonService<T, ID> {

    private PersonRepository repository;

    public PersonService(@Autowired PersonRepository repository) {
        this.repository=repository;
    }


}
