/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.easymock.EasyMock;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jbpm.formbuilder.server.file.FileService;

public class RESTFileServiceTest extends RESTAbstractTest {

    //test happy path of RESTFileService.deleteFile(...)
    public void testDeleteFileOK() throws Exception {
        RESTFileService restService = new RESTFileService();
        List<Object> requestMocks = createRequestMocks();
        FileService fileService = EasyMock.createMock(FileService.class);
        fileService.deleteFile(EasyMock.same("somePackage"), EasyMock.same("myFile.tmp"));
        EasyMock.expectLastCall().once();
        requestMocks.add(fileService);
        restService.setFileService(fileService);
        Object[] mocks = requestMocks.toArray();
        EasyMock.replay(mocks);
        Response resp = restService.deleteFile((HttpServletRequest) mocks[0], "somePackage", "myFile.tmp");
        EasyMock.verify(mocks);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.NO_CONTENT);
        
    }
    
    //test response to a FileException of RESTFileService.deleteFile(...)
    public void testDeleteFileServiceProblem() throws Exception {
        RESTFileService restService = new RESTFileService();
        List<Object> requestMocks = createRequestMocks();
        FileService fileService = EasyMock.createMock(FileService.class);
        FileException exception = new FileException("Something went wrong");
        fileService.deleteFile(EasyMock.same("somePackage"), EasyMock.same("myFile.tmp"));
        EasyMock.expectLastCall().andThrow(exception).once();
        requestMocks.add(fileService);
        restService.setFileService(fileService);
        Object[] mocks = requestMocks.toArray();
        EasyMock.replay(mocks);
        Response resp = restService.deleteFile((HttpServletRequest) mocks[0], "somePackage", "myFile.tmp");
        EasyMock.verify(mocks);
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test happy path for RESTFileService.getFiles(...) returning files
    public void testGetFilesOK() throws Exception {
        RESTFileService restService = new RESTFileService();
        List<Object> requestMocks = createRequestMocks();
        FileService fileService = EasyMock.createMock(FileService.class);
        List<String> retval = new ArrayList<String>();
        retval.add("myFile1.tmp");
        retval.add("myFile2.tmp");
        EasyMock.expect(fileService.loadFilesByType(EasyMock.same("somePackage"), EasyMock.same("tmp"))).
            andReturn(retval);
        requestMocks.add(fileService);
        restService.setFileService(fileService);
        Object[] mocks = requestMocks.toArray();
        EasyMock.replay(mocks);
        Response resp = restService.getFiles((HttpServletRequest) mocks[0], "somePackage", "tmp"); 
        EasyMock.verify(mocks);
        Object objDto = assertXmlOkResponse(resp);
        assertTrue("objDto should be of type FileListDTO", objDto instanceof FileListDTO);
        FileListDTO dto = (FileListDTO) objDto;
        assertNotNull("dto.getFile() shouldn't be null", dto.getFile());
        for (String file : dto.getFile()) {
            assertTrue("retval should contain " + file, retval.contains(file));
        }
        for (String file : retval) {
            assertTrue("dto.getFile() should contain " + file, dto.getFile().contains(file));
        }
    }
    
    //test happy path for RESTFileService.getFiles(...) returning no files
    public void testGetFilesEmpty() throws Exception {
        RESTFileService restService = new RESTFileService();
        List<Object> requestMocks = createRequestMocks();
        FileService fileService = EasyMock.createMock(FileService.class);
        List<String> retval = new ArrayList<String>();
        EasyMock.expect(fileService.loadFilesByType(EasyMock.same("somePackage"), EasyMock.same("tmp"))).
            andReturn(retval);
        requestMocks.add(fileService);
        restService.setFileService(fileService);
        Object[] mocks = requestMocks.toArray();
        EasyMock.replay(mocks);
        Response resp = restService.getFiles((HttpServletRequest) mocks[0], "somePackage", "tmp"); 
        EasyMock.verify(mocks);
        Object objDto = assertXmlOkResponse(resp);
        assertTrue("objDto should be of type FileListDTO", objDto instanceof FileListDTO);
        FileListDTO dto = (FileListDTO) objDto;
        assertNotNull("dto.getFile() shouldn't be null", dto.getFile());
        assertTrue("dto.getFile() should be empty", dto.getFile().isEmpty());
    }
    
    //test response to a FileException of RESTFileService.getFiles(...)
    public void testGetFilesServiceProblem() throws Exception {
        RESTFileService restService = new RESTFileService();
        List<Object> requestMocks = createRequestMocks();
        FileService fileService = EasyMock.createMock(FileService.class);
        FileException exception = new FileException("Something going wrong");
        EasyMock.expect(fileService.loadFilesByType(EasyMock.same("somePackage"), EasyMock.same("tmp"))).
            andThrow(exception);
        requestMocks.add(fileService);
        restService.setFileService(fileService);
        Object[] mocks = requestMocks.toArray();
        EasyMock.replay(mocks);
        Response resp = restService.getFiles((HttpServletRequest) mocks[0], "somePackage", "tmp"); 
        EasyMock.verify(mocks);
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test happy path for RESTFileService.getFile(...)
    public void testGetFileOK() throws Exception {
        RESTFileService restService = new RESTFileService();
        List<Object> requestMocks = createRequestMocks();
        FileService fileService = EasyMock.createMock(FileService.class);
        byte[] myContent = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
        EasyMock.expect(fileService.loadFile(EasyMock.same("somePackage"), EasyMock.same("myFile.tmp"))).
            andReturn(myContent);
        requestMocks.add(fileService);
        restService.setFileService(fileService);
        Object[] mocks = requestMocks.toArray();
        EasyMock.replay(mocks);
        Response resp = restService.getFile((HttpServletRequest) mocks[0], "somePackage", "myFile.tmp");
        EasyMock.verify(mocks);
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.OK);
        assertNotNull("resp.entity shouldn't be null", resp.getEntity());
        assertNotNull("resp.metadata shouldn't be null", resp.getMetadata());
        Object contentType = resp.getMetadata().getFirst(HttpHeaderNames.CONTENT_TYPE);
        assertNotNull("resp.metadata[Content-Type] shouldn't be null", contentType);
        assertEquals("resp.metadata[Content-Type] should be application/octet-stream but is " + contentType, 
                contentType, MediaType.APPLICATION_OCTET_STREAM);
        Object objDto = resp.getEntity();
        assertTrue("objDto should be an array", objDto.getClass().isArray());
        assertTrue("objDto should be a byte array", objDto instanceof byte[]);
        byte[] retval = (byte[]) objDto;
        assertEquals("retval should be the same as " + myContent + " but is " + retval, retval, myContent);
    }
    
    //test response to a FileException for RESTFileService.getFile(...)
    public void testGetFileServiceProblem() throws Exception {
        RESTFileService restService = new RESTFileService();
        List<Object> requestMocks = createRequestMocks();
        FileService fileService = EasyMock.createMock(FileService.class);
        FileException exception = new FileException("Something going wrong");
        EasyMock.expect(fileService.loadFile(EasyMock.same("somePackage"), EasyMock.same("myFile.tmp"))).
            andThrow(exception);
        requestMocks.add(fileService);
        restService.setFileService(fileService);
        Object[] mocks = requestMocks.toArray();
        EasyMock.replay(mocks);
        Response resp = restService.getFile((HttpServletRequest) mocks[0], "somePackage", "myFile.tmp");
        EasyMock.verify(mocks);
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }

    private List<Object> createRequestMocks() {
        List<Object> requestMocks = new ArrayList<Object>();
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(request.getSession()).andReturn(session).once();
        EasyMock.expect(session.getServletContext()).andReturn(context).once();
        requestMocks.add(request);
        requestMocks.add(session);
        requestMocks.add(context);
        return requestMocks;
    }
}
