## 1. 기획

사용자 인증은 소셜 로그인을 JWT와 연동하여 구현 하는 것으로 기획 하였습니다.

소셜 로그인을 사용한 이유는 회원 가입 시 비밀번호나 개인 정보 등의 관리를 최소화 하기 위해서였습니다.

소셜 로그인의 핵심구현은 달라지지 않을 것이기 때문에 구글 연동만해도 충분하다고 판단하여 진행했습니다.

JWT를 사용한 이유는 기본적으로 앱으로 동작하는 것을 생각했기 때문에 로그인 유지와 추후 서버 확장 시 관리가 편리할 것으로 판단하여 사용했습니다.

## 2. 기능 구현

우선 구글 소셜 로그인을 연동하기 위해 구글 클라우드 콘솔에 접속해서 프로젝트를 생성한 뒤 도메인을 설정하고 클라이언트 ID 및 비밀 키 정보를 복사해두었습니다.

**.properties**

```java
google.client-name=google
spring.security.oauth2.client.registration.google.client-id=CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=SECRET
```

- 구글 클라우드 애플리케이션의 클라이언트 ID, 비밀 키를 .properties 파일에 넣어주었습니다.

**SecurityConfig.java**

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// oauth2 login 핸들러 설정
		http.oauth2Login(oauth2 -> oauth2.successHandler(
                authenticationSuccessHandler()));

		// accessToken 체크 필터
    http.addFilterBefore(
            tokenCheckFilter(jwtUtil),
            UsernamePasswordAuthenticationFilter.class
    );
		
		// refreshToken 필터
    http.addFilterBefore(new RefreshTokenFilter("/refreshToken", jwtUtil),
            TokenCheckFilter.class);            
            
    // 그외 필터 설정...

    return http.build();
}

@Bean
public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return new CustomSocialLoginSuccessHandler(jwtUtil, usersRepository);
}
```

- Spring Security Filter 설정입니다.
- oauth2 login 핸들러와 jwt 토큰 필터를 설정해주었습니다.
- accessToken 필터는 매 요청마다 accessToken을 체크합니다.
- refreshToken 필터는 accessToken 만료 시 refreshToken을 체크합니다

**OAuth Handler**

```java
@Override
public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    OAuthSecurityDTO principal = (OAuthSecurityDTO) authentication.getPrincipal();
    String email = authentication.getName();
    String platform = principal.getPlatform();

    Map<String, Object> claim = Map.of("email", email, "platform", platform);
    String accessToken = jwtUtil.generateToken(claim, 1);
    String refreshToken = jwtUtil.generateToken(claim, 10);

    response.addCookie(createCookie("accessToken", accessToken));
    response.addCookie(createCookie("refreshToken", refreshToken));

    boolean exist = usersRepository.existsById(new UsersId(email, platform));
    response.addCookie(createCookie("registered", String.valueOf(exist)));

    response.sendRedirect(redirectURL);
}
```

- 이메일과 플랫폼(소셜)을 이용하여 JWT 토큰을 생성합니다.
- 생성된 토큰을 쿠키에 담아 Frontend로 리다이렉트 합니다.
- registered는 소셜 로그인 후 최초 접속 여부를 확인하기 위해 추가했습니다
    - 최초 접속: 사용자 프로필 입력 페이지로 이동
    - else: 메인 페이지로 이동

**Frontend**

```javascript
// 리다이렉트 페이지
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
Cookies.remove('refreshToken');
Cookies.remove('accessToken');

// axios 설정
instance.interceptors.request.use(
  async config => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);
```

- 프론트엔드에서 쿠키를 localStorage로 옮깁니다.
    - 쿠키는 매 요청마다 서버로 전송되는 것으로 알고 있었고 이 때문에 refreshToken이 매 요청마다 서버로 전송되는 것을 방지하기 위함
- 이후 axios의 interceptor를 사용하여 API 요청마다 accessToken을 같이 전송하도록 설정하였습니다.