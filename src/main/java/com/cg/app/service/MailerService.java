package com.cg.app.service;

import java.io.IOException;
import java.net.Proxy;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cg.app.domain.Email;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
//@RefreshScope
public class MailerService {
	@Autowired
	private RestTemplate resttemplate;
	
	@PostConstruct
    public void init(){
        
		SimpleClientHttpRequestFactory clientHttpReq = new SimpleClientHttpRequestFactory();
		clientHttpReq.setProxy(Proxy.NO_PROXY);
		resttemplate.setRequestFactory(clientHttpReq);
    }

	@Value("${mail.utility.url}")
	private String mailUtilityUrl;

	public void mailThroughUitility(Email email) throws IOException {

		MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
		ObjectMapper mapper = null;
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpHeaders jsonHeader = new HttpHeaders();
		jsonHeader.setContentType(MediaType.APPLICATION_JSON);
		mapper = new ObjectMapper();
		String emailJson = mapper.writeValueAsString(email);
		HttpEntity<String> jsonPart = new HttpEntity<>(emailJson, jsonHeader);
		// putting the two parts in one request
		multipartRequest.add("data", jsonPart);
		multipartRequest.add("file", null);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, header);
		resttemplate.postForObject(mailUtilityUrl, requestEntity, String.class);

	}
}