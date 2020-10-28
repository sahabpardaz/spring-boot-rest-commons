# Spring Boot REST Commons
This library contains out-of-the-box and ready to used modules for enabling some general cross-functional features required by a typical spring-boot REST server application: auditing, request/response logging, security and API error mapping. These are separate documents provided for each of these topics:

- [API Error Mapping](doc/api-error-mapping.md): helps you on easily returning rich responses for errors on REST APIs in SpringBoot applications
- [Security](doc/security.md): does a custom security configuration suitable for a typical REST API in SpringBoot applications.
- [Request Logging](doc/request-log.md): enables complete HTTP request and response logging in SpringBoot applications.
- [Auditing](doc/custom-auditing.md): does out-of-the-box auditing for the Hibernate entities in SpringBoot applications. By auditing we mean keeping track of changes to a table rows (or entities as an ORM concept).


 ## Add it to your project

You can reference to this library by either of java build systems (Maven, Gradle, SBT or Leiningen) using snippets from this jitpack link:
[![](https://jitpack.io/v/sahabpardaz/clean-up.svg)](https://jitpack.io/#sahabpardaz/clean-up)
