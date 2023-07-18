package com.emmsale.login.application;

import com.emmsale.login.application.dto.GithubAccessTokenRequest;
import com.emmsale.login.application.dto.GithubAccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GithubClient {

  @Value("${github.client.id}")
  private String clientId;
  @Value("${github.client.secret}")
  private String clientSecret;
  @Value("${github.url.access-token}")
  private String accessTokenUrl;
  @Value("${github.url.profile}")
  private String profileUrl;

  public String getAccessTokenFromGithub(final String code) {
    final GithubAccessTokenRequest githubAccessTokenRequest = new GithubAccessTokenRequest(
        code,
        clientId,
        clientSecret
    );

    final HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

    final HttpEntity httpEntity = new HttpEntity(githubAccessTokenRequest, headers);
    final RestTemplate restTemplate = new RestTemplate();

    final String accessToken = restTemplate
        .exchange(accessTokenUrl, HttpMethod.POST, httpEntity, GithubAccessTokenResponse.class)
        .getBody()
        .getAccessToken();

    if (accessToken == null) {
      //TODO: 커스텀 예외 정의
      throw new IllegalArgumentException("code가 유효하지 않습니다.");
    }
    return accessToken;
  }
}
