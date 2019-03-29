/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.transformation;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Tags({"example"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class jsonTransformation extends AbstractProcessor {

    @Override
    protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(final String propertyDescriptorName) {
        return new PropertyDescriptor
                .Builder()
                .name(propertyDescriptorName)
                .required(false)
                .addValidator(StandardValidators.ATTRIBUTE_KEY_PROPERTY_NAME_VALIDATOR)
                .dynamic(true)
                .expressionLanguageSupported(true)
                .build();
    }

    public static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("Successful transformation").build();

    public static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("Transformation failure").build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;
    protected ObjectMapper mapper;
    private FlowFile flowFile;
    private ObjectNode jsonReturn;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);

        mapper = new ObjectMapper();
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            return;
        }

        try {
            flowFile = session.write(flowFile, new StreamCallback() {
                @Override
                public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
                    try (final InputStream bufferedIn = new BufferedInputStream(inputStream)) {
                        // get a FildNames and jsonPath Expression for generate new Context
                        Map<String, String> dynamicProperties = getDynamicProperties(context);

                        JsonNode inputJson = mapper.readTree(bufferedIn);

                        jsonReturn = mapper.createObjectNode();

                        dynamicProperties.forEach( (key, value) -> {
                            String valor = JsonPath.parse(inputJson.toString()).read(value);
                            jsonReturn.put(key, valor);
                        });

                        System.out.println(jsonReturn.toString());


                        IOUtils.write(jsonReturn.toString(), outputStream);
                    }

                }
                });
            session.transfer(flowFile, REL_SUCCESS);
        } catch (Exception e) {
            getLogger().error("Failed while processing transformation", e);
            session.transfer(flowFile, REL_FAILURE);
        }

    }

    private Map<String, String> getDynamicProperties(ProcessContext context) {
        Map<String, String> dynamicProperties = new HashMap<>();
        for (PropertyDescriptor entry : context.getProperties().keySet()) {
            if (entry.isDynamic()) {
                dynamicProperties.putIfAbsent(entry.getName(), context.getProperty(entry).evaluateAttributeExpressions(flowFile).getValue());
            }
        }

        return dynamicProperties;
    }



}
