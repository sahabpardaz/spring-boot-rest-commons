package ir.sahab.rest.common;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.ResultHandler;

/**
 * Utility class which helps generating REST documentation snippets from unit tests.
 */
public class RestDocs {

    private final String authHeaderName;

    public RestDocs(String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }

    /**
     * Generates common documentation snippets. For example, in a unit test that checks a GET method on path "/form",
     * you can call this:
     * <pre class="code">
     * mockMvc.perform(get("/form")).andDo(document("dpi/add"));
     * </pre>
     *
     * @param identifier Identifies the generated snippets path.
     *     You should add it in project's index.adoc to reference the generated snippets. For example, you can include
     *     the snippets with identifier "dpi/add" by this statement:
     *     <pre class="code">
     *     include::{snippets}/dpi/add/auto-section.adoc[]
     *     </pre>
     */
    public ResultHandler document(String identifier) {
        List<String> snippets = new ArrayList<>();
        snippets.add("auto-links");
        snippets.add("auto-description");
        snippets.add("auto-method-path");
        snippets.add("auto-path-parameters");
        snippets.add("auto-request-fields");
        snippets.add("auto-request-parameters");
        snippets.add("auto-response-fields");
        snippets.add("curl-request");
        snippets.add("http-response");
        snippets.add("request-headers");
        return result -> {
            JacksonResultHandlers.prepareJackson(new ObjectMapper()).handle(result);
            MockMvcRestDocumentation.document(identifier,
                    Preprocessors.preprocessRequest(prettyPrint()),
                    Preprocessors.preprocessResponse(
                            ResponseModifyingPreprocessors.replaceBinaryContent(),
                            ResponseModifyingPreprocessors.limitJsonArrayLength(new ObjectMapper()),
                            prettyPrint()),
                    CliDocumentation.curlRequest(),
                    HttpDocumentation.httpRequest(),
                    HttpDocumentation.httpResponse(),
                    AutoDocumentation.requestFields(),
                    AutoDocumentation.requestHeaders(),
                    AutoDocumentation.responseFields(),
                    AutoDocumentation.pathParameters(),
                    AutoDocumentation.requestParameters(),
                    AutoDocumentation.description(),
                    AutoDocumentation.embedded(),
                    AutoDocumentation.authorization(authHeaderName),
                    AutoDocumentation.links(),
                    AutoDocumentation.methodAndPath(),
                    AutoDocumentation.sectionBuilder().snippetNames(snippets).skipEmpty(false).build(),
                    requestHeaders(headerWithName(authHeaderName).description("Authorization credentials"))
            ).handle(result);
        };
    }
}
