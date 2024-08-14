## 1. 개요

배포를 완료하고 제대로 돌아가는 것을 확인했으니 다음으로 GitHub에서 특정 작업 수행 시 (태그 생성 or 메인 브랜치에 푸시) 자동으로 배포 작업이 수행 되도록 구성해보았습니다.

## 2. GitHub workflows 작성

```yaml
on:
  push:
    tags:
      # 태그가 생성될 때 실행한다
      - '*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    # AWS 인증
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v1
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ secrets.AWS_REGION }}
        
    # 배포 실행
    - name: Deploy to EC2
      env:
        PRIVATE_KEY: ${{ secrets.EC2_SSH_PRIVATE_KEY }}
        HOST: ${{ secrets.EC2_HOST }}
        USER: ec2-user
      run: |
        echo "$PRIVATE_KEY" > private_key.pem && chmod 400 private_key.pem
        ssh -o StrictHostKeyChecking=no -i private_key.pem ${USER}@${HOST} '
          cd ./[project_name] &&
          git fetch --all &&
          git checkout main &&
          git pull &&
          ./gradlew clean build --exclude-task test
```

- AWS 인증을 수행하여 접속
- 배포 스크립트 실행
    - private key를 사용하여 ssh 로 인스턴스에 접속
    - 프로젝트 폴더로 이동
    - git fetch 및 main branch로 체크아웃
    - 소스 업데이트 (pull)
    - 프로젝트 빌드

위와 같은 스크립트를 사용해서 GitHub에서 태그 생성 시 인스턴스에서 빌드까지 자동으로 실행되도록 하였습니다.
