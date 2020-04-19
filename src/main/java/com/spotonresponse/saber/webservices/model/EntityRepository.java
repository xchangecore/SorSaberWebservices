package com.spotonresponse.saber.webservices.model;


import com.amazonaws.services.dynamodbv2.document.Item;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface EntityRepository extends CrudRepository<Entity, EntityKey> {

    List<Entity> findAll();

    List<Entity> findAllByTitle(String title);

    Entity findByKey(EntityKey key);

}


