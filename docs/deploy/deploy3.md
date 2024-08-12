## 1. 개요

배포는 완료했지만 이후 개선이 필요하다고 생각한 부분이 몇 가지 있었습니다.

1. **JAR 파일을 직접 실행해야 함**
2. **인스턴스 연결 종료 시 프로세스도 종료됨**
3. 요청 시 포트번호(8080)를 계속 붙여줘야 함

우선 위의 2개의 문제를 개선하기 위해서 간단한 명령어로 프로세스를 실행하고 중단할 수 있도록 실행 스크립트를 작성하였습니다.

## 2. 스크립트 작성

### start.sh

```jsx
#!/bin/bash

JAR=~/[project_directory]/build/libs/[project_name]-0.0.1-SNAPSHOT.jar
LOG=~/[project_directory]/[project_name].log

nohup java -jar $JAR > $LOG 2>&1 &
```

- 프로젝트 디렉토리는 사용자 디렉토리 아래 위치해있고 빌드 완료 후 build/libs 디렉토리에 생성된 JAR 파일을 실행합니다.
- log는 프로젝트 디렉토리에 프로젝트.log 파일로 관리합니다.
- nohup 명령어를 사용하여 JAR을 백그라운드(&)로 실행시키는 명령어를 작성합니다.

### stop.sh

```jsx
#!/bin/bash

APP_PID=$(ps -ef | grep java | grep [project_name] | awk '{print $2}')

if [ -z "$APP_PID" ];
then
    echo "APP is not running"
else
    kill -9 $APP_PID
    echo "APP stopped."
fi
```

- ‘java’와 [project_name]으로 검색된 프로세스가 있으면 해당 프로세스를 종료합니다.

### 스크립트 실행 권한 추가 및 실행

```bash
# 스크립트 실행 권한 추가
chmod +x start.sh stop.sh

# 시작
./start.sh

# 중지
./stop.sh 
```

- 작성한 스크립트에 대해 실행 권한을 부여하고 스크립트를 실행합니다.

### GitHub workflows 수정

```jsx
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
          ./gradlew clean build --exclude-task test &&
          ./stop.sh &&
          ./start.sh
        `
```

- 이전에 작성했던 workflows에 스크립트 실행 명령어를 추가합니다.
- 실행 중인 프로세스를 먼저 중지한 다음 프로세스를 다시 시작합니다.

위와 같이 스크립트를 작성하여 배포 과정을 조금 더 간소화 할 수 있었습니다.