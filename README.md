# Onion Calendar
순수 Java로 구현하는 캘린더 (비난 양파 & 도로롱)

## 실행 방법

### 가장 쉬운 방법
Team-9 폴더에서 **`run.bat`을 더블클릭**한다. 컴파일과 실행을 한 번에 한다.

### 직접 명령어로 하기 (PowerShell, Team-9 폴더에서)

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java -Path src).FullName
java -cp out com.onioncalendar.Main
```

| 부분 | 뜻 |
| --- | --- |
| `javac` | 자바 컴파일러. `.java`(사람이 읽는 코드)를 `.class`(JVM이 실행하는 파일)로 바꾼다 |
| `-encoding UTF-8` | 소스 파일이 UTF-8이라고 알려준다. 빠지면 한글이 깨지거나 에러가 난다 |
| `-d out` | 만들어진 `.class` 파일을 `out` 폴더에 넣는다 |
| `(Get-ChildItem -Recurse -Filter *.java -Path src).FullName` | `src` 아래 모든 `.java` 파일 경로 목록을 만들어 `javac`에 넘긴다 |
| `java` | 컴파일된 프로그램을 실행한다 |
| `-cp out` | class를 찾을 위치(classpath)가 `out`이라고 알려준다 |
| `com.onioncalendar.Main` | `main` 메서드가 있는 시작 클래스(패키지 이름 포함) |

- Java 17 이상이 필요하다 (`java -version`으로 확인).
- 실행하면 `data/` 폴더에 일정이 저장된다. `data/`와 `out/`은 git에 올라가지 않는다.
- 초기화하려면 `data` 폴더를 지운다.