package com.jcrawleydev.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.jcrawleydev.demo.model.Album;

@Controller
public class AlbumsController {


	@Autowired OAuth2AuthorizedClientService oauth2ClientService;
	@Autowired RestTemplate restTemplate;
	@Autowired WebClient webClient;
	
	
	@GetMapping("/albums")
	public String getAlbums(Model model, @AuthenticationPrincipal OidcUser principal, Authentication authentication) {

		OidcIdToken idToken = principal.getIdToken();
		String idTokenValue = idToken.getTokenValue();
		System.out.println("idTokenValue : " + idTokenValue);
		
		String url = "http://localhost:8082/albums";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", getJwtTokenFrom(authentication));
		HttpEntity<Album> entity = new HttpEntity<>(headers);
		
		ParameterizedTypeReference<List<Album>> typeRef =  
				new ParameterizedTypeReference<List<Album>>() {};
		
		ResponseEntity<List<Album>> responseEntity = restTemplate.exchange(url,
				HttpMethod.GET, entity, typeRef);

		List<Album> albums = responseEntity.getBody();	
		model.addAttribute("albums", albums);
		
		return "albums";
	}
	
	
	
	@GetMapping("/webclient/albums")
	public String getAlbumsWithWebClient(Model model, @AuthenticationPrincipal OidcUser principal) {
		
		String url = "http://localhost:8082/albums";
		ParameterizedTypeReference<List<Album>> typeRef =  new ParameterizedTypeReference<List<Album>>() {};

		List<Album> albums = webClient.get()
				.uri(url)
				.retrieve()
				.bodyToMono(typeRef)
				.block();
		
		model.addAttribute("albums", albums);
		
		return "albums";
	}
	
	
	private String getJwtTokenFrom(Authentication auth) {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
        if(oauthToken == null) {
        	System.out.println("OauthToken is null!");
        }
        else if(oauthToken.getName() == null) {
        	System.out.println("oauth Token name is null!");
        }
        
        OAuth2AuthorizedClient oauth2Client
        = oauth2ClientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), 
                                                oauthToken.getName());
        if(oauth2Client == null) {
        	System.out.println("OAuth2Client object is null!");
        }
        return "Bearer " + oauth2Client.getAccessToken().getTokenValue();
	}
	
	
}
