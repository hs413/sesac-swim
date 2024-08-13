## 1. 기획

프로젝트 기획에서 사용자의 운동 데이터 수집을 어떻게 할 것 인가에 대한 고민이 있었습니다.

1. 모바일 기기를 통한 실시간 데이터 수집..
2. 모바일 앱을 통해 수집된 데이터를 API등을 사용하여 연동
3. 사용자가 데이터를 직접 입력

위의 3가지 방법 정도가 제시되었는데 1번과 2번은 안드로이드 개발이 필요했기 때문에 저는 3번으로 의견을 제시 했었습니다. 하지만 다수의 팀원이 모바일 기기를 통해 데이터 연동을 해야 한다고 주장 하였고 1번은 사실상 불가능한 수준이었기 때문에 2번으로 타협하여 데이터 연동 기능을 개발하기로 하였습니다.

또한 팀원 모두 모바일 프로그래밍에 대한 지식이나 경험 등이 전무 했기에 우선 IOS를 제외하고 Android에서 최소한의 기능만 구현 하기로 합의 하였습니다.

## 2. Health Connect

사용자의 운동 데이터 수집을 위해 생각했던 방법은 다음과 같습니다

1. 기존의 모바일 건강 앱을 통해 데이터를 수집한다.
2. API or SDK 등을 통해 해당 앱에서 데이터를 가져온다

이를 위해서 우선 기존의 건강 앱에서 데이터를 제공해주는 방법이 있는지 확인이 필요했고 리서치 결과 안드로이드의 헬스 커넥트를 이용하여 기존의 건강 앱으로부터 데이터를 일부 받아 올 수 있다는 것을 확인하여 이를 활용해보기로 했습니다.

헬스 커넥트를 사용하는 방법은 다음과 같습니다.

1. 헬스 커넥트를 실행한다.
2. 헬스 커넥트에서 기존의 건강 앱과 개발 앱의 접근 권한을 모두 허용한다.
3. 접근 권한을 허용한 시점부터 건강 앱의 운동 데이터 일부가 헬스 커넥트로 자동으로 저장된다.
4. 헬스 커넥트에 저장된 데이터를 개발 앱에서 접근하여 가져온다.

위와 같은 과정이 사용성 측면에서 좋지 않다고 생각했지만 모바일 기기로부터 데이터를 수집할 수 있는 방법에 한계가 있었기 때문에 그대로 진행하게 되었습니다.

## 3. 개발 환경

- 안드로이드 스튜디오를 설치하여 개발 환경을 세팅했습니다.
- 언어는 kotlin을 사용했고 이유는 Android 예시 코드가 대부분 kotlin으로 되어있어서 였습니다.
- 테스트 환경은 스마트폰의 개발자 모드를 활성화하여 테스트하였습니다.
    - 가상 머신을 사용하지 않은 이유는 다음과 같습니다.
        1. 노트북의 메모리 이슈..
        2. 운동 데이터 수집을 위해 실제 스마트폰에 쌓인 운동 데이터 필요

## 4. 기능 구현

우선 기능이 잘 구현이 되는지 확인 여부가 중요했기 때문에 핵심적인 기능만 간단하게 안드로이드 개발을 최소화하여 구현해보기로 했습니다.

데이터 수집은 다음과 같은 흐름으로 진행됩니다.

1. 안드로이드에서 웹 뷰를 사용하여 웹 프론트엔드를 띄워준다.
2. 프론트엔드에서 데이터 수집 요청 시그널을 보낸다.
3. 안드로이드에서 헬스 커넥트에 저장된 데이터를 가져온다.
4. 데이터를 백엔드 서버로 전송한다..

웹 뷰를 사용하여 프론트엔드를 띄워주는 이유는 안드로이드 개발을 최소화 하기 위함 입니다.

안드로이드는 데이터 수집 요청을 받은 경우 헬스 커넥트를 조회하여 데이터를 서버로 보내주는 최소한의 역할만 가지도록 구현했습니다.

**데이터 수집 요청 - Frontend**

```javascript
const getHealthConnectData = () => {
  healthConnectJsInterface.getDataRecords('2024-06-01T12:00:00.000');
};
```

- 프론트엔드에서 데이터 수집 요청을 보내는 메서드입니다.
- 매개 변수는 가장 마지막으로 데이터를 수집한 날짜로 데이터 수집의 시작 날짜가 됩니다.

**데이터 수집 이벤트 핸들러 - Android**

```kotlin
// 이벤트 핸들러 (웹에서 해당 메서드 호출)
@JavascriptInterface
fun getDataRecords(date: String) {
    CoroutineScope(Dispatchers.IO).launch {
        val response = healthConnectService.getRecords(date)
        // ... 데이터 처리 
    }
}
```

- 프론트엔드에서 보낸 요청을 받는 메서드입니다.
- 데이터 조회 메서드를 호출하여 데이터를 가져 온 뒤
- 데이터 전송을 위한 처리를 합니다.

**데이터 조회 - Android**

```kotlin
// 데이터 조회 메서드
suspend fun getRecords(date: String): ReadRecordsResponse<ExerciseSessionRecord> {
    val endTime = Instant.now()
    val ldt = LocalDateTime.parse(date)
    val startTime = ldt.atZone(ZoneId.systemDefault()).toInstant()

    val response =
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
            )
        )
    return response
}
```

- 데이터 조회 메서드입니다.
- 현재 시간을 종료일로 매개 변수로 받은 날짜를 시작일로하여 데이터를 조회합니다.

**데이터 전송 - Android**

```kotlin
fun sendDataToServer(dataList: List<SendData>) {
    api.sendData(dataList).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            return
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            return
        }
    })
}

data class SendData (
    val value: Double,
    val stopTime: String,
    val startTime: String,
    val category: String
)

interface ApiService {
    @POST("record/")
    fun sendData(@Body data: List<SendData>): Call<Void>
}
```

- 데이터 전송을 위한 메서드와 클래스입니다.
- SendData 클래스는 Java의 DTO와 같다고 생각하면 됩니다.
- sendData interface를 이용하여 서버로 POST 요청을 보냅니다.

**데이터 저장 - Backend**

```java
// controller
@PostMapping("/")
public void postExerciseRecord(@RequestBody RecordDataDTO dataDTO) {
    exerciseRecordService.register(dataDTO.getData());
}

// service
public void register(List<ExerciseRecordDTO> dataDTO) {
    List<ExerciseRecord> list = dataDTO.stream().map(data ->
            ExerciseRecord.builder()
                    .category(data.getCategory())
                    .value(data.getValue())
                    .startTime(data.getStartTime())
                    .stopTime(data.getStopTime())
                    .build()
    ).toList();
    
    recordRepository.saveAll(list);
}
```

- 백엔드에서 데이터를 받아 저장하는 로직입니다.

위와 같은 과정으로 데이터 수집이 가능하다는 것을 확인하였고 우선 이대로 기능 구현을 마무리 하였습니다.