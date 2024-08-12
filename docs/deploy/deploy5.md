## 1. 개요

처음에는 AWS 클라우드 서비스를 이용하여 배포하였습니다. 그러나 수업으로 프리티어를 모두 소진하여 프로젝트 배포 후 비용이 계속 증가 하였기 때문에 플랫폼 이전을 선택하게 되었습니다.

플랫폼은 GCP(Google Cloud Platform)으로 선택했고, 이유는 3개월 동안 300 달러 크레딧을 제공해주고 서비스의 제한 없이 사용할 수 있어 자유롭게 테스트 해볼 수 있다는 장점이 있었습니다.

## 2. 이전 과정

### VM 인스턴스 생성

- 이미지는 Ubuntu, 인스턴스 유형은 범용 E2로 AWS와 비슷한 구성으로 생성했습니다.
- AWS의 보안그룹과 같은 설정은 방화벽 설정을 통해 설정했습니다.
    - HTTP, HTTPS 모두 접속 허용했습니다.
- 인스턴스 생성 후 방화벽 설정에서 8080 포트로 접속이 가능하도록 설정했습니다.

나머지 과정은 AWS 배포 과정과 같습니다.

### GitHub Actions workflows 수정

```bash
name: Deploy to GCP on tag

on:
  push:
    tags:
      - '*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Setup Google Cloud CLI
        uses: google-github-actions/setup-gcloud@v1
        with:
          service_account_key: ${{ secrets.GCP_SA_KEY }}
          project_id: ${{ secrets.GCP_PROJECT_ID }}

      - name: Deploy to GCP
        env:
          PRIVATE_KEY: ${{ secrets.GCP_SSH_PRIVATE_KEY }}
          HOST: ${{ secrets.GCP_HOST }}
          USER: ${{ secrets.GCP_USER }}
        run: |
          echo "$PRIVATE_KEY" > private_key && chmod 400 private_key
          ssh -i private_key -o StrictHostKeyChecking=no ${USER}@${HOST} '
            cd ./[project_directory] &&
            git fetch --all &&
            git checkout main &&
            git pull &&
            ./gradlew clean build --exclude-task test && 
            ./stop.sh && 
            ./start.sh
          '
```

- workflow 설정은 인증 부분이 달라졌습니다.
- 마찬가지로 태그가 생성될 때 실행되며 GCP 인증 후 접속하여 배포 관련 스크립트들을 실행합니다.

당연하게도 AWS에서 제공하는 서비스들을 비슷하게 지원하기 때문에 이전 과정에서의 어려움은 크게 없었습니다.