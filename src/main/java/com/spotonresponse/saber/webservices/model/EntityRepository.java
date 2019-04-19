package com.spotonresponse.saber.webservices.model;


import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface EntityRepository extends CrudRepository<Entity, EntityKey> {

    List<Entity> findAll();

}

