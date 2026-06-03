# Hibernate and H2

## Hibernate

Hibernate is a **Java ORM (Object-Relational Mapper)** — it translates between Java objects and relational database tables so you don't write raw SQL for most operations.

### How it maps Java to SQL

You annotate entity classes to describe the mapping:

```java
@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    private String status;
}
```

When you persist, query, or update that object, Hibernate generates the corresponding SQL automatically:

| Java operation | Generated SQL |
|---|---|
| `em.persist(job)` | `INSERT INTO jobs (status) VALUES (?)` |
| `em.find(Job.class, 1L)` | `SELECT * FROM jobs WHERE id = 1` |
| Modify a managed entity inside a transaction | `UPDATE jobs SET status = ? WHERE id = ?` |
| `em.remove(job)` | `DELETE FROM jobs WHERE id = ?` |

### JPQL

Instead of writing table/column names, you query against class and field names:

```java
"SELECT j FROM Job j WHERE j.status = :status"
```

Hibernate translates this to SQL at runtime using the configured **Dialect** (e.g. `PostgreSQLDialect`, `H2Dialect`), which handles database-specific syntax differences.

### Dirty checking

Hibernate tracks the state of every managed entity at load time. On transaction commit it compares current vs. original state and automatically emits `UPDATE` statements for any fields that changed — no explicit save call needed.

### Spring Data JPA

In a Spring Boot project, Spring Data JPA sits on top of Hibernate. You declare a repository interface and Spring generates the implementation:

```java
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(String status);
}
```

Spring Boot auto-configures Hibernate as the JPA provider when `spring-boot-starter-data-jpa` is on the classpath.

---

## H2

H2 is a **lightweight, pure-Java relational database** that can run entirely in memory. It is not a production database — it is used for local development and testing.

### Why use it in tests

- No external process to install or start.
- The database is created fresh when the application context starts and discarded when it stops — tests are always isolated.
- Hibernate's `H2Dialect` generates standard SQL that closely mirrors PostgreSQL, so the same entity code works against both.

### How it is configured in this project

`src/test/resources/application.yml` overrides the production datasource:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
```

| Property | Meaning |
|---|---|
| `jdbc:h2:mem:testdb` | In-memory database named `testdb` (lives only in the JVM process). |
| `DB_CLOSE_DELAY=-1` | Keep the database open as long as the JVM is running. |
| `ddl-auto: create-drop` | Hibernate creates the schema on startup and drops it on shutdown. |

### Activating the test profile locally

```powershell
# Run with the test profile to use H2 instead of PostgreSQL
java -jar target/centroid-finder-1.0-SNAPSHOT.jar --spring.profiles.active=test
```

Or for Maven tests (already the default for the test classpath):

```powershell
mvn test
```

---

## How they work together in this project

```
Spring Boot
  └── Spring Data JPA (repository interfaces)
        └── Hibernate (ORM / SQL generation)
              ├── PostgreSQL (production — via SPRING_DATASOURCE_URL)
              └── H2 in-memory (tests — via src/test/resources/application.yml)
```

The `Job` entity and `JobRepository` are written once. Hibernate and the active datasource handle the rest.
