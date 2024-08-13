## 프로젝트 개요

### 개요

사용자의 수준에 맞춰 맞춤형 훈련(연습) 정보를 생성하여 제공해주는 서비스

### 선정 이유

흔히 많이 하는 러닝, 헬스 관련 어플 많이 존재하지만 수영 쪽으로는 공급이 거의 없으나 실제 수영 커뮤니티에서 ‘자유수영 루틴’ 관련 정보를 얻고자 하는 이들이 많기 때문에 이와 관련된 앱을 개발해보고자 함

[GitHub Organization](https://github.com/SwimmingTutor)

## 프로젝트 설계

### 주요 요구사항

1. 사용자의 운동 데이터를 수집하고 분석한다.
2. 수집된 운동 데이터를 기반으로 사용자의 수영 수준을 평가한다.
3. 사용자의 수영 수준에 맞춰 맞춤형 훈련 정보를 제공해준다.

### 데이터베이스

<img src="https://github.com/user-attachments/assets/f0178f29-2187-4aa0-ac2a-b778a09d9641" width="700">

### 아키텍처
<img width="1078" alt="아키텍처" src="https://github.com/user-attachments/assets/4d69f78d-a120-4223-b026-143fc53861c5">

## 작업 내역
해당 프로젝트에서 제가 담당한 작업은 다음과 같습니다
- 데이터 수집 기능 구현
- 사용자 인증 기능 구현
- 서비스 배포

### 데이터 수집
[데이터 수집](./docs/data/data1.md)<br/>

[//]: # ([사용자 인증 정보 연동]&#40;./docs/data/data2.md&#41;)

### 사용자 인증
[사용자 인증](./docs/auth/auth1.md) <br/>
[CustomArgumentResolver](./docs/auth/data2.md) <br/>


### 서비스 배포
[Backend 첫 배포](./docs/deploy/deploy1.md) <br/>
[GitHub Actions 배포 자동화](./docs/deploy/deploy2.md) <br/>
[실행 및 중지 스크립트 작성](./docs/deploy/deploy3.md) <br/>
[포트포워딩 및 SSL 적용](./docs/deploy/deploy4.md) <br/>
[클라우드 플랫폼 이전](./docs/deploy/deploy5.md) <br/>

