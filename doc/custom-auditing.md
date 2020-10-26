# Custom Auditing

## The Problem it Solves

This library does out-of-the-box auditing for the Hibernate entities in SpringBoot applications.
By auditing we mean keeping track of changes to a table rows (or entities as an ORM concept).
That means you want to answer these questions: What was the changes (all types of add, delete and update) in a table
rows during a specific time period?, and you want to know **who** applied each of those changes.
Besides, those historic data it is also useful to have fast and simple access to "created by", "created date",
"last modified by" and "last modified date" of each of those table rows (entities).

## Sample Usage

Here we mention the simple steps you should follow to enable auditing on an entity in your SpringBoot application.

### Configure SpringBoot

First, you should introduce and enable this library in your SpringBoot application by adding the `@EnableCustomAuditing`
annotation:

```java
@SpringBootApplication
@EnableCustomAuditing(schema = "audit")
public class Application {
   ...
}

```

The argument schema indicates the database name in which the audit tables should be created.

### Configure the entities you want to audit

Then you should introduce the entities you want to audit. Suppose you have an entity named Product:

```java
@Entity
public class Product {
   private int id;
   private String name;
   ...
}
```

It is sufficient to add @Audited annotation and inheritance from Auditable class:

```java
@Entity
@Audited
public class Product extends Auditable {
   private int id;
   private String name;
   private String color;
   ...
}
```

Auditable class adds the `createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate` and `version` fields to this
entity.
@Audited annotation, in the other hand causes creation of a new table: `Product_AUD` which stores the historical data,
whenever you commit a transaction. This is done by a library called [Hibernate Envers](https://docs.jboss.org/envers/docs).
Instead of annotating the whole class and auditing all properties, you can annotate only some persistent properties with
@Audited. This will cause only these properties to be audited. You can use Envers client to lookup the historical data.
For example, consider that the color of a product with ID 24 is changed, and you want to know its original color:

```java
List<Integer> revisions = reader.getRevisions(Product.class, entityId);
Product productFirstRevision = reader.find(Product.class, 24 /*Product ID*/, revisions.get(0));
System.out("Original color: " + productFirstRevision.getColor());
```

You can also access to the user that has changed a revision, or the date it has changed via revision entity:

```java
String username = reader.findRevision(RevisionEntity.class, revisions.get(0)).getUsername();
```

Using audit reader, you have many facilities for searching/navigating on historical data. You can query for all entities
at a given revision (a snapshot of data at given point in time), you can query the revisions at which entities of a
given class is changed, you can also apply filters/orders based on entity fields, ... . See the documentation of
[Hibernate Envers](https://docs.jboss.org/envers/docs) for more details.

## What we have done

The question is what does this library exactly do? We know that it uses Envers library but what does it do itself? The
answer is just some customization and configuration:

- It provides the Auditable entity which is an abstract entity you can extend to add auditing metadata to your entities.
- It adds the field `userName` to the default revision table. Revision table is a global table handled by Envers. In
fact each transaction to an audited table, causes one insertion on its corresponding `AUD` table and another insertion
to the global revision table that both have the same revision number. This way, revision number is a global number that
indicates a certain point in time. We have added `userName` to revision table to be able to find out which user has
changed an exact revision on an entity. Finding the current user is not a hard thing in Spring. We read it from Spring
security context.
- It fills the audit fields (`createdBy`, `createdDate`, `lastModifiedBy`, `lastModifiedDate` and `version`) whenever
there is a commit on any audited entities.
- It provides a ready-to-use Spring's bean of type `AuditReader`.
- It provides an easy-to-use annotation for SpringBoot to configure all of these: `@EnableCustomAuditing`