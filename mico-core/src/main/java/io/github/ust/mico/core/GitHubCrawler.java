package io.github.ust.mico.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.eclipse.egit.github.core.DownloadResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.DataInput;
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

    private final RestTemplate restTemplate;

    public GitHubCrawler(RestTemplateBuilder restTemplateBuilder){
        this.restTemplate = restTemplateBuilder.build();
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

    public void restTemplateRestCall(String uri){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
        System.out.println("Response: \n" + responseEntity);

        //TODO: Parse directly to a service object
        //Service service = restTemplate.getForObject(uri,Service.class);
        //System.out.println("Service: \n" + service);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            JsonNode root = mapper.readTree(responseEntity.getBody());
            System.out.println("Root: \n" + root);


            System.out.println("Id: \n" + root.get("id"));
            System.out.println("Name: \n" + root.get("name"));
            System.out.println("Description: \n" + root.get("description"));

            //Service service = mapper.convertValue(root, Service.class);
            //System.out.println("Service: \n" + service);



        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void urlConnectionHttpCall(String uri){
        URL url;
        HttpURLConnection con;
        StringBuffer content;
        ObjectMapper mapper;
        Service service;

        try {
            url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            System.out.println("Status: " + status);



            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            content = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

            System.out.println(content);



            mapper = new ObjectMapper();
            //service = mapper.readValue(,Service.class);



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
