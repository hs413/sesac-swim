## 1. 개요

이 장에서는 아직 처리되지 못한 포트포워딩 작업과 SSL 설정 추가에 대해 설명하겠습니다.

## 2. 포트포워딩

- 우선 nginx를 설치하고 설정 파일이 있는 디렉토리로 이동하여 .conf 파일을 수정합니다.

    ```bash
    server {
            listen 80;
            server_name localhost;
    
            location / {
                    proxy_pass http://localhost:8080;
                    proxy_set_header X-Real-IP $remote_addr;
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                    proxy_set_header Host $http_host;
            }
    }
    ```

- 80 포트로 들어오는 요청에 대해 8080 포트로 포워딩 해주도록 파일을 수정합니다.
- 파일 저장 후 nginx를 재시작해주면 이후 8080 포트를 붙이지 않아도 요청이 가능해집니다.

## 3. SSL 설정

- certbot과 python3-certbot-nginx를 이용하여 무료로 ssl을 발급 받아 사용했습니다.

```bash
/etc/letsencrypt/live/domain.com/fullchain.pem
/etc/letsencrypt/live/domain.com/privkey.pem
```

- certbot을 이용하여 ssl을 발급받으면 위와 같은 경로에 인증서가 생성됩니다.
- 인증서와 도메인이 준비되면 nginx.conf 파일을 수정합니다.
    - 도메인은 테스트 용으로 저렴한 도메인을 구매하여 사용했습니다.

```bash
server {
        listen 80;
        server_name domain.com;
        rewrite        ^ https://$server_name$request_uri? permanent;
}

server {
	listen 443 ssl;
        server_name domain.com;

	ssl_certificate /etc/letsencrypt/live/domain.com/fullchain.pem; 
        ssl_certificate_key /etc/letsencrypt/live/domain.com/privkey.pem; 
        include /etc/letsencrypt/options-ssl-nginx.conf; 

        location / {
                proxy_pass http://localhost:8080;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header Host $http_host;
        }
}
 
```

- 80 포트로 접속 시 443 포트로 접속하도록 url을 재작성 시켜줍니다.
- 그리고 다시 8080포트로 이동하도록 포트포워딩 시켜줍니다.

위와 같은 방법으로 간단하게 SSL과 포트포워딩을 처리하였습니다.