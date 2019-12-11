package com.spotonresponse.saber.webservices.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.google.cloud.datastore.*;
import com.spotonresponse.saber.webservices.model.EntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;



@Configuration
@EnableAsync
@EnableDynamoDBRepositories(basePackageClasses = EntityRepository.class)
public class DynamoDBConfig {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(DynamoDBConfig.class);
    @Value(value = "${google.cloud.aws-uuid}")
    private String DynamoDbUUID;

    private String aws_access_key_id = "";
    private String aws_secret_access_key = "";

    private String amazon_endpoint = "";
    private String amazon_region = "";
    private static String db_table_name = "";


    public AWSCredentialsProvider amazonAWSCredentialsProvider() {
        return new AWSStaticCredentialsProvider(amazonAWSCredentials());
    }


    @Bean
    public AWSCredentials amazonAWSCredentials() {
        logger.debug("Fetching credentials... why?");
            try {
                Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("Credentials")
                        .setFilter(StructuredQuery.PropertyFilter.eq("UUID", DynamoDbUUID))
                        .build();

                QueryResults<Entity> results = datastore.run(query);
                Entity entity = results.next();
                aws_access_key_id = entity.getString("username");
                aws_secret_access_key = entity.getString("password");
                amazon_endpoint = entity.getString("Endpoint");
                amazon_region = entity.getString("Region");
                db_table_name = entity.getString("TableName");

                logger.info("**************Got aws_key: " + aws_access_key_id);

            } catch (Exception ex) {
                logger.error("Error: " + ex);

            }

        return new BasicAWSCredentials(aws_access_key_id, aws_secret_access_key );
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return DynamoDBMapperConfig.DEFAULT;
    }

    @Bean
    public static String getDb_table_name() {
        return db_table_name;
    }

    /*
    @Primary
    @Bean
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB);
    }

    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig config) {
        return new DynamoDBMapper(amazonDynamoDB, config);
    }
*/


    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard().withCredentials(amazonAWSCredentialsProvider())
                .withRegion(amazon_region).build();
    }
}
