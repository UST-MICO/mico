package io.github.ust.mico.core;

import org.eclipse.egit.github.core.DownloadResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GitHubCrawler {

    @Autowired
    private ServiceRepository serviceRepository;

    private GitHubClient client;

    private GitHubRequest request;

    private DownloadResource resource;

    private GitHubResponse response;

    public GitHubCrawler(){
        client = new GitHubClient();
        request = new GitHubRequest();
        resource = new DownloadResource();
    }

    public void downloadResource (String gitHubRepo){
        request.setUri(gitHubRepo);
        try {
            response = client.get(request);
            System.out.println(client.getUser());
        } catch (IOException e) {
            e.printStackTrace();
        }
        resource.getDescription();
        System.out.println(response);
        System.out.println(response.getFirst());
        System.out.println(response.getLast());
        System.out.println(response.getNext());
        System.out.println(response.getPrevious());
        System.out.println(response.getBody());
    }

    public void simpleHttpCall(String uri){
        RestTemplate restTemplate = new RestTemplate();
        Service service = restTemplate.getForObject(uri, Service.class);
        System.out.println(service);
    }

    public void anotherHttpCall(String uri){
        URL url;

        HttpURLConnection con;

        try {
            url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            System.out.println("Status: " + status);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            System.out.println(content);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
