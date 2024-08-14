## 1. 문제 인식 - 사용자 인증 정보

처음 데이터 수집 기능을 구현할 때는 데이터 수집 및 전송의 구현 가능 여부를 판단하기 위해 핵심 기능만 구현했습니다.

1. 데이터 수집 요청을 보낸다. - 프론트엔드
2. 데이터를 조회한다. - 안드로이드
3. 데이터를 전송한다. - 안드로이드
4. 데이터를 저장한다. - 백엔드

위와 같이 데이터 수집과 전송에만 포커스를 맞춰 작업을 하였는데 문제는 데이터를 안드로이드에서 백엔드로 전송할 때 사용자 인증 정보가 포함되어 있지 않아 해당 데이터가 어떤 사용자의 데이터인지 확인이 불가능 하다는 점이 있었습니다.

## 2. 해결 방안1 - 안드로이드 중심

데이터 전송 기능을 수정하지 않고 위 문제를 해결하기 위해 다음과 같은 방법을 생각해 보았습니다.

1. 안드로이드에서 사용자 인증 정보를 처리한다.
2. 프론트엔드에서 사용자 인증 정보(토큰)을 데이터 요청 시 안드로이드로 넘겨 주고 안드로이드에서 데이터 전송 시 해당 토큰을 사용한다.

<img src="https://github.com/user-attachments/assets/6e71ace7-e92d-4704-a267-1af1f9b92dce" width="500">


두 가지 해결 방안 모두 안드로이드에서 백엔드로 데이터를 전송한다는 흐름을 수정하지 않고 사용자 인증 정보를 위한 기능을 안드로이드에서 추가하여 처리하는 방법이었습니다.

문제는 안드로이드에서 추가 개발이 필요했기 때문에 기능 개발에 부담이 있었고 프론트엔드와 안드로이드 두 곳에서 인증 정보를 관리를 해야 하기 때문에 보안 적으로 좋지 않을 것이라고 생각했습니다.

## 3. 해결 방안2 - 프론트엔드 중심

데이터 전송 로직을 수정하지 않고는 문제를 쉽게 해결할 수 없을 것이라고 판단하여 생각을 바꿔서 데이터를 프론트엔드로 반환하여 프론트엔드에서 데이터를 전송하는 방법을 생각하게 되었습니다.

<img src="https://github.com/user-attachments/assets/71668213-7316-4691-b235-72c3d19cc56f" width="500">

이 방법은 안드로이드의 추가 개발이 필요하지 않고 데이터를 프론트에 반환하도록 수정만 하면 되기 때문에 간단하게 문제를 해결할 수 있을 것이라고 생각했습니다.

## 4. 기능 수정

**Android**

```jsx
@JavascriptInterface
fun getDataRecords(date: String) {
    CoroutineScope(Dispatchers.IO).launch {
        val response = healthConnectService.getRecords(date)

        val jsonString = Gson().toJson(response)
        withContext(Dispatchers.Main) {
		        webView.evaluateJavascript("javascript:receiveDataFromKotlin('$data')", null)
        }
    }
}
```

- 조회한 데이터를 제이슨 형태의 문자열로 변환하여 프론트엔드로 반환합니다.

**Frontend**

```jsx
function useWebViewBridge() {
  const sendDataToServer = useCallback(async parsedData => {
    try {
      const response = await customAxios.post('record/test', { data: parsedData.flat() });
      console.log('Server response:', response.data);
    } catch (error) {
      console.error('Error sending data to server:', error);
    }
  }, []);

  useEffect(() => {
    window.receiveDataFromKotlin = data => {
      try {
        sendDataToServer(JSON.parse(data));
      } catch (error) {
        console.error('Error parsing data from Kotlin:', error);
      }
    };

    return () => {
      delete window.receiveDataFromKotlin;
    };
  }, [sendDataToServer]);

  return null;
}
```

- android로부터 데이터를 전달 받는 함수와 서버로 데이터를 전송하는 함수를 추가했습니다.

프론트엔드에서 API요청 시 JWT토큰을 사용하여 인증과 사용자 정보를 전송하기 때문에 간단한 방법으로 문제를 해결하였습니다.
