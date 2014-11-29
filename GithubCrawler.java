/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pipinan.githubcrawler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

/**
 *
 * @author Administrator
 */
public class GithubCrawler {

    private GitHub github = null;

    public GithubCrawler(String yourname, String password) throws IOException {
        github = GitHub.connectUsingPassword(yourname, password);
    }

    /**
     * 
     * @param username the owner name of respoitory
     * @param reponame the name of respoitory
     * @param path which folder would you like to save the zip file
     * @throws IOException 
     */
    public void crawlRepoZip(String username, String reponame, String path) throws IOException {
        GHRepository repo = github.getRepository(username+"/"+reponame);

        HttpClient httpclient = getHttpClient();
        //the url pattern is https://github.com/"USER_NAME"/"REPO_NAME"/archive/master.zip
        HttpGet httpget = new HttpGet("https://github.com/"+username+"/"+reponame+"/archive/master.zip");
        HttpResponse response = httpclient.execute(httpget);
        try {
            System.out.println(response.getStatusLine());
            if (response.getStatusLine().toString().contains("200 OK")) {

                //the header "Content-Disposition: attachment; filename=JSON-java-master.zip" can find the filename
                String filename = null;
                Header[] headers = response.getHeaders("Content-Disposition");
                for (Header header : headers) {
                    System.out.println("Key : " + header.getName()
                            + " ,Value : " + header.getValue());
                    String tmp = header.getValue();
                    filename = tmp.substring(tmp.lastIndexOf("filename=") + 9);
                }

                if (filename == null) {
                    System.err.println("Can not find the filename in the response.");
                    System.exit(-1);
                }

                HttpEntity entity = response.getEntity();

                BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                String filePath = path + filename;
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                int inByte;
                while ((inByte = bis.read()) != -1) {
                    bos.write(inByte);
                }
                bis.close();
                bos.close();

                EntityUtils.consume(entity);
            }            
        } finally {
            
        }

    }

    /**
     * Just to avoid the ssl exception when using HttpClient to access https url
     * @return 
     */
    private HttpClient getHttpClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, new SecureRandom());

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(socketFactory).build();

            return httpClient;

        } catch (Exception e) {
            e.printStackTrace();
            return HttpClientBuilder.create().build();
        }
    }

}
