package org.wso2.carbon.connector.integration.test.sharepoint;
/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.connector.integration.test.base.ConnectorIntegrationTestBase;
import org.wso2.connector.integration.test.base.RestResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class SharepointConnectorIntegrationTest extends ConnectorIntegrationTestBase {
    private Map<String, String> esbRequestHeadersMap = new HashMap<String, String>();

    private Map<String, String> apiRequestHeadersMap = new HashMap<String, String>();

    /**
     * Set up the environment.
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        init("sharepoint-connector-1.0.3-SNAPSHOT");
        esbRequestHeadersMap.put("Accept-Charset", "UTF-8");
        esbRequestHeadersMap.put("Content-Type", "application/json; odata=verbose");

        apiRequestHeadersMap.put("Accept-Charset", "UTF-8");
        apiRequestHeadersMap.put("Content-Type", "application/json; odata=verbose");
        apiRequestHeadersMap.put("Accept", "application/json; odata=verbose");
        String accessToken = connectorProperties.getProperty("accessToken");
        apiRequestHeadersMap.put("Authorization", "Bearer " + accessToken);
    }

    /**
     * Positive test case for createList method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"},
            description = "sharepoint{createList} integration test with mandatory parameters.")
    public void testCreateListWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createList");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "createListMandatory.json");
        String listId = esbRestResponse.getBody().getJSONObject("d").getString("Id");
        connectorProperties.setProperty("listId", listId);
        String itemType = esbRestResponse.getBody().getJSONObject("d")
                                         .getString("ListItemEntityTypeFullName");
        connectorProperties.setProperty("itemType", itemType);

        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists/GetByTitle('" +
                connectorProperties.getProperty("ListTitle") + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(connectorProperties.getProperty("ListTitle"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Title"));
    }

    /**
     * Positive test case for getListByTitle method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"testCreateListWithMandatoryParameters"},
            description = "sharepoint{getListByTitle} integration test with mandatory parameters.")
    public void testGetListByTitleWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getListByTitle");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "getListByTitleMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists/GetByTitle('" +
                connectorProperties.getProperty("ListTitle") + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("d").getString("Title"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Title"));
    }

    /**
     * Negative test case for getListByTitle method.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, description = "sharepoint {getListByTitle} integration " +
                                               "test negative case.")
    public void testGetListByTitleWithNegativeCase() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getListByTitle");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                                    "getListByTitleNegative.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists/GetByTitle('" +
                connectorProperties.getProperty("NegListTitle") + "')";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 404);
        Assert.assertEquals(esbRestResponse.getBody().getString("error"),
                            apiRestResponse.getBody().getString("error"));
    }

    /**
     * Positive test case for getListById method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"testCreateListWithMandatoryParameters"},
            description = "sharepoint{getListById} integration test with mandatory parameters.")
    public void testGetListByIdWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getListById");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "getListByIdMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists(guid'" +
                connectorProperties.getProperty("listId") + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("d").getString("Id"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Id"));
    }

    /**
     * Negative test case for getListById method.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, description = "sharepoint {getListById} integration test negative " +
                                               "case.")
    public void testGetListByIdWithNegativeCase() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getListById");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getListByIdNegative.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists(guid'" +
                connectorProperties.getProperty("NeglistId") + "')";
        RestResponse<JSONObject> apiRestResponse =
                sendJsonRestRequest(apiEndPoint, "GET", apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 400);
    }

    /**
     * Positive test case for retriveLists method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, description = "sharepoint{retriveLists} integration test " +
                                               "with mandatory parameters.")
    public void testRetriveListstWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:retriveLists");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                                                                       "retriveListsMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
    }

    /**
     * Positive test case for createListItem method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 2, groups = {"wso2.esb"},
            dependsOnMethods = {"testCreateListWithMandatoryParameters"},
            description = "sharepoint{createListItem} integration test with mandatory parameters.")
    public void testCreateListItemWithMandatoryParameters() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createListItem");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "createListItemMandatory.json");
        String itemId = esbRestResponse.getBody().getJSONObject("d").getString("ID");
        connectorProperties.setProperty("itemId", itemId);
        String itemType = esbRestResponse.getBody().getJSONObject("d").
                getJSONObject("__metadata").getString("type");
        connectorProperties.setProperty("itemType", itemType);
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/lists/GetByTitle('" +
                connectorProperties.getProperty("ListTitle") + "')" +
                             "/items(" + connectorProperties.getProperty("itemId") + ")";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(connectorProperties.getProperty("listItemTitle"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Title"));
    }

    /**
     * Positive test case for getItemById method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"testCreateListItemWithMandatoryParameters"},
            description = "sharepoint{getItemById} integration test with mandatory parameters.")
    public void testGetItemByIdWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getItemById");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "getItemByIdMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/lists/GetByTitle('" +
                connectorProperties.getProperty("ListTitle") + "')" +
                             "/items(" + connectorProperties.getProperty("itemId") + ")";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getJSONObject("d").getString("Id"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Id"));
    }

    /**
     * Negative test case for getItemById method.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, description = "sharepoint {getItemById} integration test negative case.")
    public void testGetItemByIdWithNegativeCase() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:getItemById");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "getItemByIdNegative.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/lists/GetByTitle('" +
                connectorProperties.getProperty("ListTitle") + "')" +
                             "/items(" + connectorProperties.getProperty("negItemId") + ")";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 400);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 400);
    }

    /**
     * Positive test case for retriveListItems method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"testCreateListItemWithMandatoryParameters"},
            description = "sharepoint{retriveListItems} integration test with mandatory parameters.")
    public void testRetriveListItemsWithMandatoryParameters() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:retriveListItems");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                                                                       "retriveListItemsMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists/getbytitle('"
                + connectorProperties.getProperty("ListTitle") + "')/Items";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
    }

    /**
     * Positive test case for updateListItems method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"testCreateListItemWithMandatoryParameters"},
            description = "sharepoint {updateListItems} integration test with mandatory parameters.")
    public void testUpdateListItemsWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updateListItems");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                                    "updateListItemsMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/lists/GetByTitle('" +
                connectorProperties.getProperty("ListTitle") + "')" +
                             "/items(" + connectorProperties.getProperty("itemId") + ")";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 204);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(connectorProperties.getProperty("updateListItemTitle"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Title"));
    }

    /**
     * Positive test case for deleteListItem method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 2, groups = {"wso2.esb"},
            dependsOnMethods = {"testUpdateListItemsWithMandatoryParameters"},
            description = "sharepoint{deleteListItem} integration test with mandatory parameters.")
    public void testDeleteListItemtWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteListItem");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "deleteListItemMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/lists/GetByTitle('" +
                connectorProperties.getProperty("ListTitle") + "')" +
                             "/items(" + connectorProperties.getProperty("itemId") + ")";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 404);
    }

    /**
     * Positive test case for updateList method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(groups = {"wso2.esb"}, dependsOnMethods = {"testCreateListWithMandatoryParameters"},
            description = "sharepoint {updateList} integration test with mandatory parameters.")
    public void testUpdateListWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updateList");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap, "updateListMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") + "/_api/web/Lists/GetByTitle('" +
                connectorProperties.getProperty("updateTitle") + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 204);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(connectorProperties.getProperty("updateTitle"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Title"));
    }

    /**
     * Positive test case for deleteList method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 2, groups = {"wso2.esb"},
            dependsOnMethods = {"testUpdateListWithMandatoryParameters"},
            description = "sharepoint{deleteList} integration test with mandatory parameters.")
    public void testDeleteListWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:deleteList");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "deleteListMandatory.json");
        String apiEndPoint = connectorProperties.getProperty("apiUrl")
                             + "/_api/web/Lists/GetByTitle('" +
                             connectorProperties.getProperty("ListTitle") + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 404);
    }

    /**
     * Positive test case for createFolder method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 1, groups = {"wso2.esb"},
            description = "sharepoint{createFolder} integration test with mandatory parameters.")
    public void testCreateFoldertWithMandatoryParameters()
            throws IOException, JSONException, InterruptedException {
        esbRequestHeadersMap.put("Action", "urn:createFolder");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "createFolderMandatory.json");
        String folderName = esbRestResponse.getBody().getJSONObject("d").getString("Name");
        connectorProperties.setProperty("folderName", folderName);
        String apiEndPoint=esbRestResponse.getBody().getJSONObject("d").getJSONObject("__metadata").
                getString("uri");
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(connectorProperties.getProperty("folderName"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Name"));
    }

    /**
     * Positive test case for retrieveFolder method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 1, groups = {"wso2.esb"},
            dependsOnMethods = {"testCreateFoldertWithMandatoryParameters"},
            description = "sharepoint{retrieveFolder} integration test with mandatory parameters.")
    public void testRetrieveFolderWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:retrieveFolder");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "retrieveFolderMandatory.json");
        String serverRelativeUrl=connectorProperties.getProperty("serverRelativeUrl").
                replaceAll(" ", "%20");
        String apiEndPoint = connectorProperties.getProperty("apiUrl") +
                             "/_api/web/GetFolderByServerRelativeUrl('" + serverRelativeUrl + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
    }

    /**
     * Positive test case for createFileWithinFolder method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 1, groups = {"wso2.esb"},
            dependsOnMethods = {"testCreateFoldertWithMandatoryParameters"},
            description = "sharepoint{createFileWithinFolder} integration test with mandatory" +
                          " parameters.")
    public void testCreateFileWithinFolderWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:createFileWithinFolder");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "createFileWithinFolderMandatory.json");
        String filePath = esbRestResponse.getBody().getJSONObject("d").getString("ServerRelativeUrl");
        connectorProperties.setProperty("filePath", filePath);
        String serverRelativeUrl=connectorProperties.getProperty("serverRelativeUrl").
                replaceAll(" ", "%20");
        String apiEndPoint = connectorProperties.getProperty("apiUrl")
                             + "/_api/web/GetFolderByServerRelativeUrl('" +
                             serverRelativeUrl + "')/Files('" +
                connectorProperties.getProperty("fileName") + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(connectorProperties.getProperty("fileName"),
                            apiRestResponse.getBody().getJSONObject("d").getString("Name"));
    }

    /**
     * Positive test case for retriveAllFilesWithinFolder method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 1, groups = {"wso2.esb"},
            description = "sharepoint{retriveAllFilesWithinFolder} integration test with mandatory " +
                          "parameters.")
    public void testRetriveAllFilesWithinFolderMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:retriveAllFilesWithinFolder");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "retriveAllFilesWithinFolderMandatory.json");
        String serverRelativeUrl=connectorProperties.getProperty("serverRelativeUrl").
                replaceAll(" ", "%20");
        String apiEndPoint = connectorProperties.getProperty("apiUrl")
                             + "/_api/web/GetFolderByServerRelativeUrl('" +
                             serverRelativeUrl + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
    }

    /**
     * Positive test case for getFile method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 1, groups = {"wso2.esb"}, description = "sharepoint{getFile} integration test" +
                                                             " with mandatory parameters.")
    public void testGetFileWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:getFile");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "getFileMandatory.json");
        String serverRelativeUrl=connectorProperties.getProperty("serverRelativeUrl").
                replaceAll(" ", "%20");
        String apiEndPoint = connectorProperties.getProperty("apiUrl")
                             + "/_api/web/GetFolderByServerRelativeUrl('" +
                             serverRelativeUrl + "')/Files('" +
                connectorProperties.getProperty("fileName") + "')/$value";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(esbRestResponse.getBody().getString("output"),
                            apiRestResponse.getBody().getString("output"));

    }

    /**
     * Positive test case for updateFileContent method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 1, groups = {"wso2.esb"},
            dependsOnMethods = {"testCreateFileWithinFolderWithMandatoryParameters"},
            description = "sharepoint {updateFileContent} integration test with mandatory parameters.")
    public void testUpdateFileContenttWithMandatoryParameters() throws IOException, JSONException {

        esbRequestHeadersMap.put("Action", "urn:updateFileContent");
        RestResponse<JSONObject> esbRestResponse =
                sendJsonRestRequest(proxyUrl, "POST", esbRequestHeadersMap,
                                    "updateFileContentMandatory.json");
        String serverRelativeUrl=connectorProperties.getProperty("serverRelativeUrl").
                replaceAll(" ", "%20");
        String apiEndPoint = connectorProperties.getProperty("apiUrl")
                             + "/_api/web/GetFolderByServerRelativeUrl('" +
                serverRelativeUrl + "')/Files('" +
                connectorProperties.getProperty("fileName") + "')/$value";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 204);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(connectorProperties.getProperty("updateCondent"),
                            apiRestResponse.getBody().getString("output"));
    }

    /**
     * Positive test case for deleteFile method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 2, groups = {"wso2.esb"},
            dependsOnMethods = {"testUpdateFileContenttWithMandatoryParameters",
                                "testGetFileWithMandatoryParameters"},
            description = "sharepoint{deleteFile} integration test with mandatory parameters.")
    public void testDeleteFileWithMandatoryParameters() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:deleteFile");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "deleteFileMandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
    }

    /**
     * Positive test case for deleteFolder method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 2, groups = {"wso2.esb"},
            dependsOnMethods = {"testDeleteFileWithMandatoryParameters",
                                "testRetriveAllFilesWithinFolderMandatoryParameters"},
            description = "sharepoint{deleteFolder} integration test with mandatory parameters.")
    public void testDeleteFolderWithMandatoryParameters() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:deleteFolder");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                "deleteFolderMandatory.json");
        String serverRelativeUrl=connectorProperties.getProperty("serverRelativeUrl").
                replaceAll(" ", "%20");
        String apiEndPoint = connectorProperties.getProperty("apiUrl")
                             + "/_api/web/GetFolderByServerRelativeUrl('" +
              serverRelativeUrl + "')";
        RestResponse<JSONObject> apiRestResponse = sendJsonRestRequest(apiEndPoint, "GET",
                                                                       apiRequestHeadersMap);
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 200);
        Assert.assertEquals(apiRestResponse.getHttpStatusCode(), 500);
    }

    /**
     * Positive test case for createCustomListItem method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 2, groups = {"wso2.esb"},
            description = "sharepoint{createCustomListItem} integration test with mandatory" +
                          " parameters.")
    public void testCreateCustomListItemMandatoryParameters() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createCustomListItem");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                                                                       "createCustomListItemMandatory.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 201);
        Assert.assertEquals(connectorProperties.getProperty("customListItemType"), esbRestResponse
                .getBody().
                getJSONObject("d").getJSONObject("__metadata").getString("type"));
    }

    /**
     * Negative test case for createCustomListItem method with mandatory parameters.
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test(priority = 2, groups = {"wso2.esb"},
            description = "sharepoint{createCustomListItem} integration test with negative parameters.")
    public void testCreateCustomListItemNegativecase() throws IOException, JSONException {
        esbRequestHeadersMap.put("Action", "urn:createCustomListItem");
        RestResponse<JSONObject> esbRestResponse = sendJsonRestRequest(proxyUrl, "POST",
                                                                       esbRequestHeadersMap,
                                                                       "createCustomListItemNegative.json");
        Assert.assertEquals(esbRestResponse.getHttpStatusCode(), 404);
    }
}
