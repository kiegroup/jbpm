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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jbpm.formbuilder.server.task.MetaDataDTO;
import org.jbpm.formbuilder.server.task.PackageAssetDTO;
import org.jbpm.formbuilder.server.task.PackageAssetsDTO;

public class GuvnorFileService {
    
    private final GuvnorHelper helper;
    private final String baseUrl;
    
    public GuvnorFileService(String url, String user, String pass) {
        this.helper = new GuvnorHelper(url, user, pass);
        this.baseUrl = url;
    }

    public String extractFileExtension(String fileName) {
        int dotInd = fileName.lastIndexOf('.');
        // if dot is in the first position, we are dealing with a hidden file rather than an extension
        return (dotInd > 0 && dotInd < fileName.length()) ? fileName.substring(dotInd + 1) : null;
    }
    
    public String stripFileExtension(String fileName) {
        int dotInd = fileName.lastIndexOf('.');
        // if dot is in the first position, we are dealing with a hidden file rather than an extension
        return (dotInd > 0) ? fileName.substring(0, dotInd) : fileName;
    }
    
    public String storeFile(String packageName, String fileName, byte[] content) throws GuvnorFileException {
        try {
            String assetName = stripFileExtension(fileName);
            String assetExt = extractFileExtension(fileName);

            deleteOlderVersion(packageName, fileName);

            HttpClient client = new HttpClient();
            PostMethod create = new PostMethod(helper.getRestBaseUrl() + packageName + "/assets/");
            try {
                create.addRequestHeader("Authorization", helper.getAuth());
                create.addRequestHeader("Content-Type", "application/octet-stream");
                create.addRequestHeader("Accept", "application/atom+xml");
                create.addRequestHeader("Slug", assetName + '.' + assetExt);
                create.setRequestEntity(new ByteArrayRequestEntity(content));
                client.executeMethod(create);
            } finally {
                create.releaseConnection();
            }
            return (this.baseUrl + "/org.drools.guvnor.Guvnor/api/packages/" + packageName + "/" + fileName);
        } catch (Exception e) {
            throw new GuvnorFileException("Problem storing file", e);
        }
    }

    private void deleteOlderVersion(String packageName, String fileName) throws GuvnorFileException {
        HttpClient client = new HttpClient();
        String assetName = stripFileExtension(fileName);
        GetMethod check = new GetMethod(helper.getRestBaseUrl() + packageName + "/assets/" + assetName);
        try {
            check.addRequestHeader("Authorization", helper.getAuth());
            check.addRequestHeader("Accept", "application/xml");
            client.executeMethod(check);
            if (check.getStatusCode() == 200) {
                deleteFile(packageName, fileName);
            }
        } catch (IOException e) {
            throw new GuvnorFileException("Problem getting old version of asset " + fileName + " in package " + packageName, e);
        } finally {
            check.releaseConnection();
        } 
    }

    public void deleteFile(String packageName, String fileName) throws GuvnorFileException {
        HttpClient client = new HttpClient();
        DeleteMethod deleteAsset = new DeleteMethod(helper.getRestBaseUrl() + packageName + "/assets/" + fileName);
        try {
            deleteAsset.addRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(deleteAsset);
        } catch (IOException e) {
            throw new GuvnorFileException("Problem deleting guvnor file", e);
        } finally {
            deleteAsset.releaseConnection();
        }
    }

    public List<String> loadFilesByType(String packageName, String fileType) throws GuvnorFileException {
        HttpClient client = new HttpClient();
        GetMethod load = new GetMethod(helper.getRestBaseUrl() + packageName + "/assets/");
        try {
            load.addRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(load);
            PackageAssetsDTO dto = helper.jaxbTransformation(PackageAssetsDTO.class, load.getResponseBodyAsStream(), PackageAssetsDTO.class, PackageAssetDTO.class, MetaDataDTO.class);
            List<PackageAssetDTO> validAssets = new ArrayList<PackageAssetDTO>();
            if (fileType != null && !"".equals(fileType)) {
                for (PackageAssetDTO asset : dto.getAsset()) {
                    if (asset.getMetadata().getFormat().equals(fileType)) {
                        validAssets.add(asset);
                    }
                }
            } else {
                validAssets.addAll(dto.getAsset());
            }
            List<String> retval = new ArrayList<String>();
            for (PackageAssetDTO asset : validAssets) {
                String refLink = asset.getRefLink();
                String fileName = refLink.substring(refLink.lastIndexOf('/') + 1);
                fileName += "." + asset.getMetadata().getFormat();
                retval.add(fileName);
            }
            return retval;
        } catch (IOException e) {
            throw new GuvnorFileException("Problem obtaining assets from guvnor", e);
        } catch (JAXBException e) {
            throw new GuvnorFileException("Problem parsing assets from guvnor", e);
        } finally {
            load.releaseConnection();
        }
    }

    public byte[] loadFile(String packageName, String fileName) throws GuvnorFileException {
        HttpClient client = new HttpClient();
        String assetName = stripFileExtension(fileName);
        GetMethod get = new GetMethod(helper.getRestBaseUrl() + packageName + "/assets/" + assetName + "/source");
        try {
            get.addRequestHeader("Authorization", helper.getAuth());
            client.executeMethod(get);
            return get.getResponseBody();
        } catch (IOException e) {
            throw new GuvnorFileException("Problem reading file " + fileName, e); 
        } finally {
            get.releaseConnection();
        }
    }
    
}
