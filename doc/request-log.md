# Custom Request Logging

## The Problem it Solves

This is a Java library based on [Zalando Logbook](https://github.com/zalando/logbook) to enable complete HTTP request
and response logging in SpringBoot Applications. In fact, it does nothing than a simple ready-to-use configuration of
the Logbook library.

## Sample Usage

First, you should introduce and enable this library in your SpringBoot application by adding the
`@EnableCustomRequestLogging` annotation:

```java
@SpringBootApplication
@EnableCustomRequestLogging(
    logLevel = "INFO",
    obfuscateHeaders = "X-Authentication",
    obfuscateParameters = "pass,access-token",
    maxBodySize = 1500)
public class Application {
   ...
}
```

There are four arguments you can configure in the `@EnableCustomRequestLogging` annotation:

- logLevel: indicates the level of the logs. If you do not provide this argument, we use `"INFO"` as the default value
although if you use Logbook directly the default log level is `"DEBUG"`.
- obfuscateHeaders: based on security reasons, we should not write credential data in logs. By this argument, we can
tell which HTTP headers should be obfuscated. The default value of this parameter is
`"Authorization,X-API-KEY,X-Auth-Token"` (These are the default headers used by HTTP basic authentication and OAuth
implementations).
- obfuscateParameters: it helps to obfuscate the credential data if they are provided in request parameters. By default,
we obfuscate "X-API-KEY" parameter that contains credential data in some OAuth implementations.
- maxBodySize: some requests or responses contain a huge body, and we do not want to pollute the log files by the full
content. We can configure to cut the logs for this kind of requests/responses by this parameter. The default value of
this parameter is 1000 (1 KB).

Note that all of these parameters, and some more configs can be set via property files too. You can see a comprehensive
list of them in [Zalando Logbook](https://github.com/zalando/logbook) documentation, But the mentioned parameters are
just the most important ones and in some applications, you do not require any further configuration. If a parameter set
both in one of the Spring property files and as the arguments to `@EnableCustomRequestLogging`, the values from code
overwrite the values in files although for the `obfuscateHeaders` and `obfuscateParameters`, we will aggregate them
(append the values from code to the values from property files).

It's time to see an example of the logs written by this library. This is an example of what we have extracted from one
of the application that uses this library:

- The request part:

```json
{
   "origin":"remote",
   "type":"request",
   "correlation":"d7e5bde33322bf81",
   "protocol":"HTTP/1.1",
   "remote":"192.168.10.10",
   "method":"GET",
   "uri":"http://192.168.10.20:8080/ui-backend/api/orders/v1?orderId=1001",
   "headers":{
      "Accept":[
         "application/json"
      ],
      "Content-Type":[
         "application/json;charset=UTF-8"
      ],
      "X-Authentication":[
         "XXX"
      ]
   }
}
```

As you can see, the original value of X-Authentication header (that is configured for obfuscation) is erased.

- The response part:

```json
{
   "origin":"local",
   "type":"response",
   "correlation":"d7e5bde33322bf81",
   "duration":100,
   "protocol":"HTTP/1.1",
   "status":200,
   "headers":{
      "Cache-Control":[
         "no-cache, no-store, max-age=0, must-revalidate"
      ],
      "Content-Type":[
         "application/json;charset=UTF-8"
      ],
      "Expires":[
         "0"
      ],
      "Pragma":[
         "no-cache"
      ],
      "X-Content-Type-Options":[
         "nosniff"
      ],
      "X-Frame-Options":[
         "DENY"
      ],
      "X-XSS-Protection":[
         "1; mode=block"
      ]
   },
   "body":{
      "id":1001,
      "name": "Tuna Fish conserve",
      "price": 20000,
      "category":"FOOD"
   }
}
```

You can use the correlation number to match a request with its corresponding response.

Also, note that if you use this library in conjunction with the [API Error Mapping](api-error-mapping.md), you will
get rich informative logs not just for successful requests/responses but also for error cases. For example this is a
response log produced for an unauthorized user trying to access a protected path:

```json
{
   "origin":"local",
   "type":"response",
   "correlation":"c735d4c872a9400f",
   "duration":59,
   "protocol":"HTTP/1.1",
   "status":403,
   "headers":{
      "Cache-Control":[
         "no-cache, no-store, max-age=0, must-revalidate"
      ],
      "Content-Type":[
         "application/problem+json;charset=UTF-8"
      ],
      "Expires":[
         "0"
      ],
      "Pragma":[
         "no-cache"
      ],
      "X-Content-Type-Options":[
         "nosniff"
      ],
      "X-Frame-Options":[
         "DENY"
      ],
      "X-XSS-Protection":[
         "1; mode=block"
      ]
   },
   "body":{
      "title":"Forbidden",
      "status":403,
      "detail":"Access is denied",
      "user":"Mehdi",
      "api_path":"/ui-backend/api/orders/v1",
      "http_method":"POST",
      "timestamp":1600846045186,
      "tracking_id":"38fd10ac-001d-4c3e-9fa4-a6b3be792976",
      "en_message":"Forbidden: Access is denied",
      "fa_message":"درخواست مربوطه با خطا مواجه شد."
   }
}
```
