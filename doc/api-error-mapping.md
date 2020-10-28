# API Error Mapping

## The Problem it Solves

This library helps you on easily returning rich responses for errors on REST APIs in SpringBoot applications.
When you call a REST API, the response you get is a HTTP response packet.  For a successful call, the status code of the
response is a 2XX code and it may contain a body (JSON or another format) containing the response content.
Error cases, in the other hand come with other status codes like 400, 403, 500, ... . Like normal cases, the error cases
can contain a body providing more information about the error, but the point is that, in REST controllers, you are more
likely to throw one subclass of the `Exception` class for an error. So they should be converted to an HTTP response.
This library uses the [Zalando Problem Spring Web](https://github.com/zalando/problem-spring-web) to convert general
exception classes to the proper informative HTTP responses. But using this library you will get more than that:

1. It enriches the error responses with several useful informational fields that are not contained in original Zalando
library like the error code, bilingual messages, timestamp, tracking ID, user, API path and HTTP method.
2. It provides an easy way to define API specific exceptions.
3. It provides an easy-to-use annotation for SpringBoot to configure all of these: `@EnableApiErrorMapping`

### Rich Error Responses

Using this library, the HTTP body of the response for the error cases is a JSON containing these fields:

- status_code: the status code which is in response header is repeated in response body too.
- timestamp: when the error is generated.
- user: the user who has called the API.
- api_path: the API path that is called.
- http_method: the HTTP method (GET, PUT, POST, DELETE, ...) of the request.
- en_message: the error message in English.
- fa_message: the error message in Farsi. (you can consider it as the second language. Farsi is language of the
programmers of this library :-))
- tracking_id: a UUID which helps in debugging. Because of the security reasons, the HTTP response does not contain the
exception stack trace but it is written in log files. We can find it by searching this tracking ID.
- error_code: a string code defined for the API specific error case. Note that this field is not filled for all types of
errors. General errors just contain the status code.
- detail: the extra information about the error. This filed is not filled for all types of errors.

Some exceptions, may also have several further fields which are specific to their context.

### Custom API Errors

You get an informative error body for most general exception classes. But what about the API specific exceptions that
are specific to your application? We will help you in this case too. We have defined a special exception class called
`ApiException` for this reason.
Throwing this type of exception from a controller method, you can specify the `en_message`, `fa_message`, `error_code`
and `status_code` which should be returned to the client. But you do not need to worry about these fields each time you
want to throw an `ApiException`. Instead you just set an `ApiErrorCode` inside the `ApiException`. So you can define the
`en_message`, `fa_message`, `error_code` and `status_code` just once and from a central class. i.e., your custom
implementation of `ApiErrorCode` interface. You can see a simple implementation example using a Java Enum in next section.

## Sample Usage

Here we mention the simple steps you should follow to enable API error mapping in your SpringBoot application.

### Configure SpringBoot

First, you should introduce and enable this library in your SpringBoot application by adding the
`@EnableApiErrorMapping` annotation:

```java
@SpringBootApplication
@EnableApiErrorMapping
public class Application {
   ...
}

```

Now, you have informative errors for general exceptions out of the box. For example consider someone calls a REST path
that does not exist. They get this body in response:

```json
{
   "title":"Not Found",
   "status":404,
   "detail":"No handler found for POST /ui-backend/api/orders/v1",
   "user":"John",
   "api_path":"/ui-backend/api/orders/v1",
   "http_method":"POST",
   "timestamp":1600783551649,
   "tracking_id":"c475e911-af18-4d45-bfcc-8305902a40ac",
   "en_message":"Not Found: No handler found for POST /ui-backend/api/orders/v1",
   "fa_message":"درخواست مربوطه با خطا مواجه شد."
}
```

Or when they do not provide a mandatory request parameter, they get something like this:

```json
{
   "title":"Bad Request",
   "status":400,
   "detail":"Required BigDecimal parameter 'amount' is not present",
   "user":"Saeed",
   "api_path":"/ui-backend/api/orders/v1",
   "http_method":"POST",
   "timestamp":1600784396612,
   "tracking_id":"689fb986-f9ca-48ff-8976-582f10c7078b",
   "en_message":"Bad Request: Required BigDecimal parameter 'amount' is not present",
   "fa_message":"درخواست مربوطه با خطا مواجه شد."
}
```

Or maybe it is an exception thrown from your controller. For example an unhandled `IOException` from the controller
method indicating there is a problem in server side. So the client sees something like this:

```json
{
   "title":"Internal Server Error",
   "status":500,
   "detail":"Connection to database is closed!",
   "user":"MohammadAli",
   "api_path":"/ui-backend/api/orders/v1",
   "http_method":"POST",
   "timestamp":1600784765639,
   "tracking_id":"613deba3-b381-48a0-b2fb-81b8a9c25b68",
   "en_message":"Internal Server Error: Connection to database is closed!",
   "fa_message":"درخواست مربوطه با خطا مواجه شد."
}
```

Or think of a `javax.validation.ConstraintViolationException`. Suppose the controller method calls the ORM and it throws
this constraint violation exception. Then the clients sees something relevant:

```json
{
   "type":"https://zalando.github.io/problem/constraint-violation",
   "title":"Constraint Violation",
   "status":400,
   "user":"Mojtaba",
   "api_path":"/ui-backend/api/orders/v1",
   "http_method":"POST",
   "timestamp":1600843801133,
   "tracking_id":"4e627b97-7ce3-4352-9fbc-2ebe093da4af",
   "en_message":"Constraint Violation",
   "fa_message":"درخواست مربوطه با خطا مواجه شد."
}
```

As you see there are many cases which are handled with careful selection of HTTP status code and the underlying
messages. These exceptions are from different kinds: routing, network and I/O, security, validation, ... .

Even if there is a totally unknown exception, we will get a reasonable response. Suppose the controller throws an
exception with type `MyException` which is unknown to this library. That's what we see in body of the response packet:

```json
{
   "title":"Internal Server Error",
   "status":500,
   "detail":"Invalid order type: URGENT",
   "user":"Hesam",
   "api_path":"/ui-backend/api/orders/v1",
   "http_method":"POST",
   "timestamp":1600844419323,
   "tracking_id":"b35d32b1-5fff-4059-819f-f78e3fbfccc9",
   "en_message":"Internal Server Error: Invalid order type: URGENT",
   "fa_message":"درخواست مربوطه با خطا مواجه شد."
}
```

### Define your Custom API Error Codes

Now it's the time to define your custom API errors. For example suppose there is a REST call for buying a product, but
we may reject it with these error codes:

- NOT_AVAILABLE_IN_STORE if there is not enough quantity of the requested product in the store.
- NOT_ENOUGH_CREDIT if the customer has not enough credit to buy the product.

So we can design our custom implementation of `ApiErrorCode` this way:

```java
public enum MyApiErrorCode implements ApiErrorCode {

    // Common error codes
    NOT_AVAILABLE_IN_STORE(Status.BAD_REQUEST, "There is not enough quantity of the requested product in the store.", "به مقدار کافی از محصول مورد نظر در انبار موجود نیست"),
    NOT_ENOUGH_CREDIT(Status.BAD_REQUEST, "You have not enough credit to buy the product!", "شما اعتبار کافی برای خرید محصول مورد نظر را ندارید");

    private final Status status;
    private final String enMessage;
    private final String faMessage;

    MyApiErrorCode(Status status, String enMessage, String faMessage) {
        this.status = status;
        this.enMessage = enMessage;
        this.faMessage = faMessage;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Status getHttpStatusCode() {
        return status;
    }

    @Override
    public String getEnMessage() {
        return enMessage;
    }

    @Override
    public String getFaMessage() {
        return faMessage;
    }
}
```

Then we can throw these errors from methods inside our controller classes:

```text
@PostMapping
public Receipt buy(@RequestBody BuyRequest request) throws ApiException {
     if (!availableInStore(request.getProduct())) {
         throw new ApiException(MyApiErrorCode.NOT_AVAILABLE_IN_STORE);
     }

     if (!enoughCredit(request.getCustomer(), request.getProduct().getPrice())) {
         throw new ApiException(MyApiErrorCode.NOT_ENOUGH_CREDIT);
     }

     // The real business...
```

And the response body in an error case will be filled like this:

```json
{
   "title":"Bad Request",
   "status":400,
   "detail":"null",
   "tracking_id":"f1d88a18-bd6d-49f0-bc83-1a4e1eb5d448",
   "en_message":"There is not enough quantity of the requested product in the store.",
   "fa_message":"به مقدار کافی از محصول مورد نظر در انبار موجود نیست",
   "error_code":"NOT_AVAILABLE_IN_STORE",
   "user":"Ali",
   "api_path":"/ui-backend/api/orders/v1",
   "http_method":"POST",
   "timestamp":1600844664969
}
```
