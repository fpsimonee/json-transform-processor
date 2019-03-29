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

import com.transformation.utils.ReadFiles;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.skyscreamer.jsonassert.JSONAssert;

import static com.transformation.jsonTransformation.REL_SUCCESS;


public class jsonTransformationTest {

    private TestRunner testRunner;
    Map<String, String> attributes;

    //test files base path
    public static final String BASE_PATH = "/src/test/resources/com/transformation";

    // read input and output files class
    private ReadFiles files;

    @Before
    public void init() {
        files = new ReadFiles();
        testRunner = TestRunners.newTestRunner(jsonTransformation.class);
        attributes = new HashMap<String, String>();
    }

    @Test
    public void getAuthorIndex0() throws IOException, JSONException {

        byte[] testDataIn = files.get(String.format("%s/input.json", BASE_PATH));
        byte[] testDataOut = files.get(String.format("%s/author-output.json", BASE_PATH));

        testRunner.setProperty("author", "$.store.book[0].author");

        MockFlowFile mockFlowFile = testRunner.enqueue(testDataIn);
        mockFlowFile.putAttributes(attributes);

        testRunner.run();

        List<MockFlowFile> ff = testRunner.getFlowFilesForRelationship(REL_SUCCESS);
        testRunner.assertAllFlowFilesTransferred(REL_SUCCESS);

        System.out.println("testDynamicProperties: ");
        System.out.println(new String(ff.get(0).toByteArray(), "UTF-8")+"\n");
        JSONAssert.assertEquals(new String(testDataOut, "UTF-8"), new String(ff.get(0).toByteArray()), true);
    }

    @Test
    public void getTitleIndex0() throws IOException, JSONException {

        byte[] testDataIn = files.get(String.format("%s/input.json", BASE_PATH));
        byte[] testDataOut = files.get(String.format("%s/title-output.json", BASE_PATH));

        testRunner.setProperty("title", "$.store.book[0].title");

        MockFlowFile mockFlowFile = testRunner.enqueue(testDataIn);
        mockFlowFile.putAttributes(attributes);

        testRunner.run();

        List<MockFlowFile> ff = testRunner.getFlowFilesForRelationship(REL_SUCCESS);
        testRunner.assertAllFlowFilesTransferred(REL_SUCCESS);

        System.out.println("testDynamicProperties: ");
        System.out.println(new String(ff.get(0).toByteArray(), "UTF-8")+"\n");
        JSONAssert.assertEquals(new String(testDataOut, "UTF-8"), new String(ff.get(0).toByteArray()), true);
    }

}
