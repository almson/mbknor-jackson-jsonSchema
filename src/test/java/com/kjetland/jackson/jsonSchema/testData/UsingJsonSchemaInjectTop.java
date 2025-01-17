package com.kjetland.jackson.jsonSchema.testData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaBool;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import java.util.Set;
import java.util.function.Supplier;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author alex
 */
public class UsingJsonSchemaInjectTop {
    @JsonSchemaInject(
        json=
          """
            {
              "patternProperties": {
                "^s[a-zA-Z0-9]+": {
                  "type": "string"
                }
              },
              "properties": {
                "injectedInProperties": "true"
              }
            }
          """,
        strings = {@JsonSchemaString(path = "patternProperties/^i[a-zA-Z0-9]+/type", value = "integer")}
    )
    public record UsingJsonSchemaInject
    (
      @JsonSchemaInject(
          json=
            """
              {
                 "options": {
                    "hidden": true
                 }
              }
            """)
      String sa,

      @JsonSchemaInject(
        json=
          """
              {
                 "type": "integer",
                 "default": 12
              }
            """,
        overrideAll = true
      )
      @Pattern(regexp = "xxx") // Should not end up in schema since we're replacing with injected
      String saMergeFalse,

      @JsonSchemaInject(
        bools = {@JsonSchemaBool(path = "exclusiveMinimum", value = true)},
        ints = {@JsonSchemaInt(path = "multipleOf", value = 7)}
      )
      @Min(5)
      int ib,

      @JsonSchemaInject(jsonSupplier = UserNamesLoader.class)
      Set<String> uns,

      @JsonSchemaInject(jsonSupplierViaLookup = "myCustomUserNamesLoader")
      Set<String> uns2
    )
    {}

    public static class UserNamesLoader implements Supplier<JsonNode> {
        ObjectMapper _objectMapper = new ObjectMapper();

        @Override public JsonNode get() {
            var schema = _objectMapper.createObjectNode();
            var values = schema.putObject("items").putArray("enum");
            values.add("foo");
            values.add("bar");

            return schema;
        }
    }

    @RequiredArgsConstructor
    public static class CustomUserNamesLoader implements Supplier<JsonNode> {
        final String custom;
        ObjectMapper _objectMapper = new ObjectMapper();

        @Override public JsonNode get() {
            var schema = _objectMapper.createObjectNode();
            var values = schema.putObject("items").putArray("enum");
            values.add("foo_"+custom);
            values.add("bar_"+custom);

            return schema;
        }
    }

    @JsonSchemaInject(
      json = """
        {
          "everything": "should be replaced"
        }""",
      overrideAll = true
    )
    public record UsingJsonSchemaInjectWithTopLevelMergeFalse
    (
      String shouldBeIgnored
    )
    {}
}
