# 구현 노트: 캘린더 · 양파 진행도 · 시간표 · 공유 · 공강시간 (이로운)

## 들어 있는 것

| 패키지 | 클래스 |
| --- | --- |
| `model` | ScheduleType, OnionStage, TimeBlock, Schedule, Lecture, ScheduleBook, Onion(진행도 계산), OnionStatus, **Friend, FriendBook** |
| `service` | StudyGoal, StudyPlanner(**임시 구현**, 현국 코드로 교체), **FreeTimeFinder, FreeTimeResult, TimeSlot** |
| `util` | Json(외부 라이브러리 없는 JSON 파서/작성기), ScheduleRepository, **ShareFileManager, FriendRepository** |
| `controller` | ScheduleListener, ScheduleController, **ShareController**, ScheduleApp |
| `view` | UiStyle, MainFrame, CalendarPanel, DayDetailDialog, ScheduleDetailDialog, ScheduleFormDialog(**임시 구현**, 현국 코드로 교체), **TimetablePanel, LectureFormDialog, SharePanel, ExportDialog, FriendInfoDialog, FreeTimePanel** |
| (루트) | Main |

데이터는 실행 폴더의 `data/schedules.json`(내 일정·수업), `data/friends.json`(가져온 친구)에 자동 저장된다. 폴더가 없으면 만든다.

## 실행 (Java 17 이상: `record`를 써서 16 미만은 안 됨)

레포 최상위에서 PowerShell로 실행한다.

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java -Path src).FullName
java -cp out com.onioncalendar.Main
```

## 현국 일정 등록 코드로 교체할 때

일정 등록(ScheduleFormDialog, StudyPlanner)은 현국이 구현한다. 아래 모양만 지키면 캘린더 쪽은 고칠 필요가 없다.

- `new ScheduleFormDialog(Window owner)`
- `Schedule showForm(Schedule s)`: s가 null이면 새 일정, 아니면 수정. 취소하면 null. 수정이면 같은 id를 유지
- `StudyGoal getGoal()`: 시험이면 공부량 입력값, 아니면 null
- 저장은 창이 아니라 호출한 쪽(CalendarPanel, ScheduleDetailDialog)이 `ScheduleController.addSchedule / addExam / updateSchedule`로 한다
- `StudyPlanner.allocate(exam, goal)`: 저장하지 않고 STUDY 일정 목록만 돌려준다. `examId = exam.getId()`

## 색상

팀 공통 팔레트(현국 MainCalendarApp)를 `UiStyle`에 적용했다. 남색 (44,62,103), 배경 (243,245,249), 테두리 (230,233,240), 유형별 배경·글자색 4쌍. [+ 일정 추가] 버튼은 과제 글자색 주황 (194,97,24)에 흰 글씨.

## 클래스 다이어그램과 다르게 구현한 것

보고서 다이어그램을 기준으로 했고, 구현하면서 바꾸거나 더한 것만 적었다. 다이어그램 반영 여부는 팀에서 정한다.

| # | 위치 | 다이어그램 | 구현 | 이유 |
| --- | --- | --- | --- | --- |
| 1 | `Onion.calcProgress` | `int` 반환 | 셀 일정이 없으면 `Onion.NO_DATA(-1)` 반환. `getStage(-1)`은 DEFAULT | "일정 없음(기본 양파)"과 "0%(썩은 양파)"를 구분해야 함 (UC11 1-1) |
| 2 | `Onion` | 상수 2개 | `WINDOW_DAYS = 14`, `NO_DATA`, `isCounted(s, today)` 추가 | 14일 기준을 한곳에 두고, 완료 수 계산에서 재사용 |
| 3 | `Onion.getBubble/getAsciiArt` | 양파 파트 | 임시 문구만 넣음 | **승민 파트**에서 채울 자리 |
| 4 | `Schedule` | getter 일부 | `getDate()`, `isTimed()`, `copy()`, setter 추가 | 과제처럼 `start`가 없는 일정의 날짜는 `end` 기준. `copy()`는 내보내기 때 원본 보호용 |
| 5 | `ScheduleBook` | `remove(s: Schedule)` | `removeLecture(l)`, `getSchedules()`, `getLectures()` 추가 | 시간표 수업 삭제와 저장에 필요 |
| 6 | `ScheduleController` | 검증 결과 `boolean` | `getLastError()` 추가 | 경고창에 "○○ 수업과 시간이 겹칩니다" 같은 이유를 보여주려면 필요 (UC2 4-2) |
| 7 | `ScheduleController.updateSchedule(s, goal)` | - | `s`는 원래 일정과 **id가 같은 새 객체**. 완료 여부는 원래 값 유지. `goal`이 null이면 공부 일정을 그대로 둠 | 수정 창에서 취소해도 원본이 바뀌지 않게 |
| 8 | `CalendarPanel` | - | `openNewSchedule()` 추가 | 상단 [+ 일정 추가] 버튼이 부를 메서드 |
| 9 | `ScheduleFormDialog` | 필드 목록 | 날짜·시작·종료·직접 설정 시작일 칸 추가, **메모 칸 없음** | Schedule에 memo 필드가 없음. 유형 선택은 다이어그램대로 콤보박스 |
| 10 | `StudyGoal(readings, solvings, startDate)` | 그대로 | 기간(3일/2주/3주/직접)은 입력 창에서 `startDate`로 바꿔서 넘김 | |
| 11 | `StudyPlanner.allocate(exam, goal)` | 현국 파트 | 임시 구현: 오늘 이전 날짜 제외, 회독 → 문제풀이 순서로 남은 날에 고르게 배치, 하루에 몰리면 "1~3회독"처럼 묶음 | 일정 등록을 먼저 돌려보기 위함. **현국 코드로 교체** |
| 12 | `UiStyle`, `Json` | 없음 | 새 클래스 | 화면 공통 색·버튼, 외부 라이브러리 없는 JSON 처리 |
| 13 | `ScheduleRepository` | load/save | JSON 변환 static 메서드 공개 | 공유 기능(ShareFileManager)에서 같은 형식을 재사용 |
| 14 | `ScheduleApp(Path dataDir)` | `start/showCalendar/exit` | 저장 폴더를 받는 생성자 추가. 트레이·시작 프로그램은 아직 연결 안 함 | 테스트용 폴더 지정. 트레이는 **승민 파트** |
| 15 | `ScheduleController` | addLecture만 | `updateLecture(old, new)`, `deleteLecture`, `getLectures` 추가. 수정이 겹치면 원래 수업을 되돌림 | 시간표 블록 클릭 → 수정/삭제 (UC2) |
| 16 | `ShareController` | SharePanel을 참조해 확인창 띄움 | `setReplaceConfirmer(Predicate<String>)`로 "덮어쓸까요?"를 화면에서 주입. `getLastError()` 추가 | 컨트롤러가 Swing에 의존하지 않게 → 테스트 가능 (UC6 중복 이름) |
| 17 | `ShareFileManager.read` | 전용 예외 클래스 | `IOException`(못 읽음) / `IllegalArgumentException`(형식 틀림, 이유 문구 포함) | 클래스 하나 줄이고 경고창 문구는 그대로 |
| 18 | 공유 파일 형식 | - | `app, version, name, exportedAt, progress, lectures, schedules`. 일정 없으면 `progress: null` | 버전으로 나중에 형식이 바뀌어도 구별. 일정 없음과 0% 구분 |
| 19 | 제목 미포함 내보내기 | 일정만 "일정 있음" | 수업 이름도 **"수업"**으로 가림. 복사본만 바꾸고 원본은 그대로 | 팀 결정 (11/25) |
| 20 | `Friend` | name, progress, lectures, schedules | `exportedAt`, `isOutdated(today)`(7일 이상), `getBlocks()` 추가 | 오래된 파일 경고 (UC7 6-1), 공강 계산 입력 |
| 21 | `FreeTimeFinder.findFreeTime` | 친구 목록을 받음 | `List<List<TimeBlock>>`(사람별 바쁜 블록) + 주 + 시간 범위를 받음. 나는 ShareController가 맨 앞에 넣음 | Friend와 나(ScheduleBook)를 같은 방식으로 다루기 위해 |
| 22 | `TimeSlot` | 요일 + 시각 | `LocalDateTime start/end` + `busy`(바쁜 사람 수) | 바로 일정으로 추가 가능, 날짜 넘어가는 주 계산 단순 |
| 23 | 추천 규칙 | - | 30분 단위, 모두 비는 1시간 이상 구간 우선, 없으면 바쁜 사람 가장 적은 구간. 지난 시간 제외, 최대 5개 | 결과가 비지 않게 (UC7 대체 흐름) |
| 24 | 추천 → 일정 추가 | 일정 등록 화면 재사용 | 제목만 묻는 간단 입력창(기본 "팀플 회의") → 개인 일정으로 저장 | 현국 화면이 아직 없어서. 교체 시 `FreeTimePanel.addAsSchedule()`만 바꾸면 됨 |
| 25 | `MainFrame` | 화면 4개 | `FREE_TIME` 카드 추가 (상단 탭은 "공유"가 켜진 상태) | 공강시간 찾기는 공유 화면에서 들어가는 하위 화면 |
| 26 | 내보내기 이름 | 매번 입력 | `Preferences`에 마지막 이름 기억 | 같은 이름으로 보내야 친구 쪽에서 덮어쓰기가 됨 |

## GUI 초안과 달라진 것

- 완료 일정은 **취소선만** 표시하고 ✓ 기호는 뺐다. 윈도우 한글 글꼴에 ✓가 없으면 네모로 깨질 수 있어서.
- 시험 칸은 "알고리즘 **시험**"처럼 뒤에 "시험"을 붙여 보여준다.
- 이전/다음 달 버튼은 40×40으로 고정했다. 좁으면 "..."로 잘리는 문제를 막기 위해서.
- 날짜 칸은 `GridLayout(0, 7)`이라 6줄짜리 달(예: 2027년 1월)도 깨지지 않는다.
- 오류 문구(분홍 상자)는 입력이 틀렸을 때만 나타난다.
- 시간표는 월~토, 기본 09~18시이고 수업이 그 밖에 있으면 범위가 자동으로 늘어난다. 수업 색은 과목 이름별로 고정.
- 수업 입력 창은 겹침 경고 뒤에도 입력한 값이 그대로 남는다.
- 친구 목록에서 7일 이상 지난 파일에는 주황 배지("7일 이상 지남")를 붙였다.
- 공강시간 화면은 바쁜 사람 수에 따라 4단계 색(초록=모두 가능 → 진한 남색=대부분 바쁨). 고른 추천 시간은 격자에 주황 테두리.

## 확인한 것

- 진행도: 일정 없음 → -1, 3/4 완료 → 75%, 오늘 미완료 일정은 제외, 15일 전 일정과 개인 일정은 제외, 단계 경계(80/79/49)
- 공부 배치: 시험 전날까지, 오늘 이전 날짜 없음, 시험 id 연결. 시험 수정 시 안 한 공부만 지우고 다시 배치, 완료한 공부는 유지
- 시험 삭제 시 안 한 공부 일정도 함께 삭제
- 수업 겹침 검사: 10:30 종료와 10:30 시작은 허용, 겹치면 수업 이름이 담긴 경고
- 저장 후 다시 불러오기(JSON 왕복)
- 내보내기: 이름 1~10자, 일정 0개면 거부, 제목 미포함 시 "일정 있음"/"수업", 원본 일정은 안 바뀜
- 가져오기: 다른 앱 파일·깨진 JSON·이름 없음 거부, 같은 이름은 확인 후 덮어쓰기, 취소하면 그대로
- 공강시간: 친구 0명/시간 범위 거꾸로 → 경고, 지난 시간 제외, 모두 비는 시간 없으면 최소 인원 추천
- 시간표 수정이 겹치면 원래 수업이 되돌아옴

## 다음 단계

1. 통합: 현국 StudyPlanner/ScheduleFormDialog 교체 (추천 → 일정 추가도 그 화면으로 바꿀지 결정)
2. 승민 Onion 그림·말풍선·트레이 연결 (FriendInfoDialog도 `Onion.getAsciiArt/getBubble`을 그대로 씀)
3. 설정 화면(아직 빈 화면)
