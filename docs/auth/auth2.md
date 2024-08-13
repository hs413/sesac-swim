## 1. 문제 인식 - 복합 키 구성

데이터베이스 모델링에서 테이블의 기본 키(식별자)는 각 레코드를 구별할 수 있는 값으로 테이블에 존재하는 값을 복합 키로 구성하는 것을 권장한다고 배웠습니다.

따라서 테이블의 기본 키는 대부분 복합 키로 구성되어 있고 그 중 소셜 로그인에서 사용되었던 이메일과 플랫폼이 대부분의 테이블의 기본 키에 포함되어 있었습니다.

즉, 데이터를 조회하기 위해선 이메일과 플랫폼 정보가 거의 항상 필요하다고 볼 수 있고 이를 API 요청마다 페이로드나 파라미터 등으로 보내주는 것이 번거롭다고 생각하여 이를 해결하기 위한 방법을 고민했습니다.

## 2. 액세스 토큰

위 문제 해결하기 위해 처음 생각했던 방법은 액세스 토큰에 정보를 담아서 보내는 것이었습니다.

액세스 토큰은 API 요청마다 인증을 위해 전송되기 때문에 해당 정보를 담아서 같이 보내면 따로 값을 추가로 보내주지 않아도 될 것이라고 생각했습니다.

**LoginSuccessHandler**

```java
Map<String, Object> claim = Map.of("email", email, "platform", platform);
String accessToken = jwtUtil.generateToken(claim, 1);
String refreshToken = jwtUtil.generateToken(claim, 10);
```

- 로그인 성공 시 토큰을 생성하는 로직입니다.
- 이메일과 플랫폼 정보를 담아서 토큰을 생성합니다.

## 3. SecurityContext

다음으로 필터에서 액세스 토큰으로 전달 받은 인증 정보를 security context에 저장해 주었습니다.

**AccessTokenFilter**

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
    try {
        Map<String, Object> claim = validateAccessToken(request);
        saveClaimToAuthentication(claim, request);
        filterChain.doFilter(request, response);
    } catch (AccessTokenException accessTokenException) {
        accessTokenException.sendResponseError(response);
    }
}

private void saveClaimToAuthentication(Map<String, Object> claim, HttpServletRequest request) {
    String token = request.getHeader("Authorization").substring(7);  // "Bearer " 제거

    // JwtAuthenticationToken 생성
    JwtAuthenticationToken authentication = new JwtAuthenticationToken(token, claim, authorities);

    // SecurityContext에 Authentication 설정
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

- 토큰의 유효성 검사를 실행합니다.
- 유효한 토큰이라면 saveClaimToAuthentication 메서드를 실행합니다.
    - security context에 인증 정보를 저장하기 위한 메서드입니다.

**JwtAuthenticationToken**

```java
	public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;
    private final Map<String, Object> principal;

    public JwtAuthenticationToken(String token, Map<String, Object> principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
```

## 4. Custom Argument Resolver

이제 컨트롤러에서 security contex에 저장된 인증 정보를 사용하기 위해 Custom Argument Resolver 구현하였습니다. (Argument Resolver는 컨트롤러 메서드에서 사용하는 파라미터를 처리하는데 사용하며 사용자가 직접 정의하여 사용 가능)

**CustomArgumentResolver**

```java
@Component
public class CustomArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null
                && parameter.getParameterType().equals(UserInfo.class);
    }
    
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
            UserInfo userInfo = new UserInfo();
            userInfo.setEmail((String) principal.get("email"));
            userInfo.setPlatform((String) principal.get("platform"));
            return userInfo;
        }
        return null;
    }
}
```

- supportsParameter()
    - 특정 파라미터를 처리할 수 있는지 여부를 판단하는 메서드입니다.
        - true를 반환하면 해당 resolver를 사용하여 값을 제공합니다.
    - @CurrentUser 애너테이션을 사용하고 매개변수 타입이 UserInfo에 맞으면 true를 반환합니다.

        ```java
        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface CurrentUser {
        }
        
        public class UserInfo {
            private String email;
            private String platform;
        }
        ```

- resolveArgument()
    - 파라미터를 실제로 해결하는 로직을 구현하는 메서드입니다.
    - security context에 저장된 사용자 인증 정보를 가져와서 반환해 주도록 구현했습니다.

**Controller & Service**

```java
// controller
@GetMapping("/profile")
public UsersProfileDTO getUserProfile(@CurrentUser UserInfo userInfo) {
    return usersService.findProfile(userInfo);
}

// service
public UsersProfileDTO findProfile(UserInfo userInfo) {
    UsersId usersId = new UsersId(userInfo.getEmail(), userInfo.getPlatform());
    
    // logic ...
}
```

- 컨트롤러와 서비스에서 사용한 예시 코드입니다
- @CurrentUser 애너테이션을 사용하여 이메일과 플랫폼에 대한 사용자 인증 정보를 가져올 수 있습니다.