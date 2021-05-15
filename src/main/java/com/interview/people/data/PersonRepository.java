package com.interview.people.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonRepository extends JpaRepository<Person, Integer>  {


    @Query("SELECT p FROM Person p where email=:email")
    Person findByEmail(@Param("email") String email);

//    Person findByEmail(String email);


}
