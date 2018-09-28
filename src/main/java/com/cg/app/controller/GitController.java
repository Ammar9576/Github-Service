package com.cg.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.cg.app.service.Getservice;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
//@RefreshScope
public class GitController {

	@Value("${cp.fileName}")
	String CP_FILENAME;

	@Value("${dmt.fileName}")
	String DMT_FILENAME;

	@Value("${dmt.Name}")
	String DMT_NAME;

	@Value("${cp.Name}")
	String CP_NAME;
	
	/*@Value("#{'${dmt.cancel}'.split(',')}")
	private List<String> dmtCancelJobURLs;
	
	@Value("#{'${cp.cancel}'.split(',')}")
	private List<String> cpCancelJobURLs;*/

	@Autowired
	Getservice getService;

	@ApiOperation("${call to update a application }")
	@GetMapping("/quarterUpdate/{APPLICATION_NAME}")
	public String type(
			@ApiParam("Mention the application to be updated(CP or DMT), and should not be blank") @PathVariable String APPLICATION_NAME) {

		APPLICATION_NAME = APPLICATION_NAME.toLowerCase();
		if (APPLICATION_NAME.equals(CP_NAME)) {
			return getService.fetchAndUpdateInGithub(CP_FILENAME,CP_NAME);
		} else if (APPLICATION_NAME.equals(DMT_NAME)) {
			return getService.fetchAndUpdateInGithub(DMT_FILENAME,DMT_NAME);
		} else {
			throw new RuntimeException("Incorrect APPLICATION NAME or the URL");
		}

	}
	
	@ApiIgnore
	@PostMapping("/client")
	public String clientTest(@RequestBody String updatedContent) throws Exception {
		return updatedContent;
	}

}
