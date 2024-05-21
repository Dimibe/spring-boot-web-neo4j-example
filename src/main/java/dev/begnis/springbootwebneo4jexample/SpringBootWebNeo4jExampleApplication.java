package dev.begnis.springbootwebneo4jexample;

import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@EnableNeo4jRepositories
@EnableNeo4jAuditing
public class SpringBootWebNeo4jExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebNeo4jExampleApplication.class, args);
    }

    @Bean
    public ApplicationRunner init(PersonRepository personRepository) {
        return args -> {
            personRepository.save(new Person("1", "John Doe"));
            System.out.println("Person saved");
        };
    }

    /**
     * This is needed to make sure that the cypher queries are generated using the dialect for Neo4j 5.0.
     */
    @Bean
    public Configuration cypherConfiguration() {
        return Configuration.newConfig().withDialect(Dialect.NEO4J_5).build();
    }

}

@Node
record Person(@Id @GeneratedValue(UUIDStringGenerator.class) String id, String name) {
}

@Repository
interface PersonRepository extends Neo4jRepository<Person, String> {
}

@RestController
class PersonController {

    private final PersonRepository personRepository;

    private final Neo4jTemplate neo4jTemplate;

    public PersonController(PersonRepository personRepository, Neo4jTemplate neo4jTemplate) {
        this.personRepository = personRepository;
        this.neo4jTemplate = neo4jTemplate;
    }

    @GetMapping("/person/repo")
    public ResponseEntity<Person> getPersonRepo() {
        return ResponseEntity.of(personRepository.findById("1"));
    }

    // THROWS EXCEPTION
    // Request processing failed: org.springframework.beans.factory.NoUniqueBeanDefinitionException:
    // No qualifying bean of type 'org.springframework.transaction.TransactionManager' available:
    // expected single matching bean but found 2: transactionManager,reactiveTransactionManager] with root cause
    @GetMapping("/person/repo/transaction")
    @Transactional(readOnly = true)
    public ResponseEntity<Person> getPersonRepoTransaction() {
        return ResponseEntity.of(personRepository.findById("1"));
    }


    @GetMapping("/person/template")
    public ResponseEntity<Person> getPersonFromTemplate() {
        var opt = neo4jTemplate.findById("1", Person.class);
        return ResponseEntity.of(opt);
    }

    // THROWS EXCEPTION
    // Request processing failed: org.springframework.beans.factory.NoUniqueBeanDefinitionException:
    // No qualifying bean of type 'org.springframework.transaction.TransactionManager' available:
    // expected single matching bean but found 2: transactionManager,reactiveTransactionManager] with root cause
    @GetMapping("/person/template/transaction")
    @Transactional(readOnly = true)
    public ResponseEntity<Person> getPersonFromTemplateWithTransaction() {
        var opt = neo4jTemplate.findById("1", Person.class);
        return ResponseEntity.of(opt);
    }
}


