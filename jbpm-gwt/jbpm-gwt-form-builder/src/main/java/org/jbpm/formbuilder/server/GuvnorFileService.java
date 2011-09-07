package org.jbpm.formbuilder.server;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class GuvnorFileService {
    
    private final GuvnorHelper helper;
    
    public GuvnorFileService(String url, String user, String pass) {
        this.helper = new GuvnorHelper(url, user, pass);
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
    
    public void storeFile(String packageName, String fileName, byte[] content) throws GuvnorFileException {
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

    public List<String> loadFiles(String packageName, String fileType) throws GuvnorFileException {
        // TODO Auto-generated method stub
        return null;
    }
    
}
