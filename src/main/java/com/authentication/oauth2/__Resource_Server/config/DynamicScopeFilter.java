package com.authentication.oauth2.__Resource_Server.config;

import com.authentication.oauth2.__Resource_Server.dto.ApiRulesResponse;
import com.authentication.oauth2.__Resource_Server.dto.Constants;
import com.authentication.oauth2.__Resource_Server.dto.Rule;
import com.authentication.oauth2.__Resource_Server.entity.ApiRule;
import com.authentication.oauth2.__Resource_Server.service.ScopeValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DynamicScopeFilter extends OncePerRequestFilter {

    @Autowired
    private ScopeValidationService scopeValidationService;

    private final ObjectMapper mapper = new ObjectMapper();
    private List<ApiRule> allApiRules;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CachedBodyHttpServletRequestWrapper cachedRequest;

        if (!request.getRequestURI().equalsIgnoreCase(Constants.REGISTER_API)) {

            allApiRules = scopeValidationService.getAllApiRules();

            if (allApiRules.isEmpty()){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied: No scope mapping found for this API");
                return;
            }

           cachedRequest = new CachedBodyHttpServletRequestWrapper(request);

            String requiredScope = findRequiredScope(cachedRequest);

            if (requiredScope == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied: No scope mapping found for this API");
                return;
            }

            // Validate JWT scope
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Missing or invalid Authorization header");
                return;
            }

            String token = authorizationHeader.substring(7);
            Jwt jwt;
            try {
                JwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                        new SecretKeySpec("qwertyuiopasdfghjklzxcvbnm123456".getBytes(), "HmacSHA256")).build();
                jwt = jwtDecoder.decode(token);
            } catch (JwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token");
                return;
            }

            List<String> tokenScopes = jwt.getClaimAsStringList("scope");
            if (tokenScopes == null || !tokenScopes.contains(requiredScope)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied: Insufficient scope");
                return;
            }

            filterChain.doFilter(cachedRequest, response);

        }
        else {
            filterChain.doFilter(request, response);
        }

    }

    private String findRequiredScope(HttpServletRequest request) throws IOException {
        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod();

        // Check dynamic APIs
        for (ApiRule apiRule : allApiRules) {
            String pathPattern = extractApiPath(apiRule.getUrl());
            String method = apiRule.getMethod();

            if (matchesPathPattern(pathPattern, requestUri) && method.equalsIgnoreCase(httpMethod)) {
                String rulesJson = apiRule.getRulesJson();
                ApiRulesResponse apiRulesResponse = mapper.readValue(rulesJson, ApiRulesResponse.class);
                return evaluateRules(request, apiRulesResponse.getRules());
            }
        }

        return null;
    }

    private String extractApiPath(String url){
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri.getPath();
    }

    private boolean matchesPathPattern(String pathPattern, String requestUri) {
        String regex = pathPattern.replace("{", "(?<").replace("}", ">[^/]+)");
        return requestUri.matches(regex);
    }

    private String evaluateRules(HttpServletRequest request, List<Rule> rules) throws IOException {
        JsonNode requestBody;

        CachedBodyHttpServletRequestWrapper cachedRequest = (CachedBodyHttpServletRequestWrapper) request;
        requestBody = extractBodyFromFormData(cachedRequest);
        if (Objects.isNull(requestBody)){
            requestBody = mapper.readTree(cachedRequest.getBody());
        }

        for (Rule rule: rules) {

            Rule.Condition condition = rule.getCondition();
            String type = condition.getType();

            switch (type) {
                case Constants.DIRECT_API:
                    if (!requestBody.isEmpty() ){
                        continue;
                    }
                    return rule.getScope();

                case Constants.QUERY_PARAM:
                    String paramKey = condition.getKey();
                    String requestParamValue = request.getParameter(paramKey);
                    if (condition.getValue().equals(requestParamValue)) {
                        return rule.getScope();
                    }
                    break;

                case Constants.REQUEST_BODY:
                    String requestBodykey = condition.getKey();
                    String requestBodyPathValue;
                    try {
                        requestBodyPathValue = JsonPath.read(requestBody.toString(), requestBodykey);
                    } catch (Exception e) {
                        continue;
                    }
                    if (condition.getValue().equals(requestBodyPathValue)) {
                        return rule.getScope();
                    }
                    break;

                case Constants.FORM_REQUEST:
                    String formRequestkey = condition.getKey();
                    String formRequestValue;
                    try {
                        formRequestValue = JsonPath.read(requestBody.toString(), formRequestkey);
                    } catch (Exception e) {
                        continue;
                    }
                    if (condition.getValue().equals(formRequestValue)) {
                        return rule.getScope();
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown condition type: " + type);
            }
        }

        return null;
    }

    private JsonNode extractBodyFromFormData(CachedBodyHttpServletRequestWrapper request) throws JsonProcessingException {

        String contentType = request.getContentType();

        if (contentType != null && (Constants.FORM_CONTENT_TYPE.equalsIgnoreCase(contentType)
                || contentType.startsWith(Constants.MULTIPART_CONTENT_TYPE))) {

            Map<String, String> dataMap = new HashMap<>();

            String req = request.getBody();
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_]+)=([a-zA-Z0-9_]+)");
            Matcher matcher = pattern.matcher(req);

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                dataMap.put(key,value);
            }

            return mapper.valueToTree(dataMap);
        }
        return null;
    }
}

