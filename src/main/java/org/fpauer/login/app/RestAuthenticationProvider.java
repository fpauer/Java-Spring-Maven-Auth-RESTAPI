package org.fpauer.login.app;

import java.io.Serializable;
import java.util.*;

import org.springframework.asm.TypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RestAuthenticationProvider implements AuthenticationProvider, Serializable {

    final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        Map<String,String> map = new HashMap<String,String>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = null;
        		
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/json");
            HttpEntity<?> request = new HttpEntity<Object>(headers);
            
            StringBuilder url = new StringBuilder();
            url.append("http://").append("127.0.1.1").append(":").append("9998").append("/auth/ldap/")
            .append(authentication.getName()).append("/").append(authentication.getCredentials().toString());

            ResponseEntity<String> response = restTemplate.exchange(url.toString(), HttpMethod.GET, request, String.class);
            
            //convert JSON string to Map
            json = mapper.readTree(response.getBody());
        } catch (HttpClientErrorException e) {
            //user logon failed !
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ( json != null && json.has("lookup") ) {
            List<GrantedAuthority> grantedAuths = new ArrayList<>();
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication auth = new UsernamePasswordAuthenticationToken(name, password, grantedAuths);
            return auth;
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
    //@Override
    //public boolean supports(Class<?> authentication) {
     //   return authentication.equals(UsernamePasswordAuthenticationToken.class);
    //}
}