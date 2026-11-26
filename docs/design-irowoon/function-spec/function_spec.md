# 함수 명세 초안 (캘린더·일정·공유 파트)

작성: 이로운 · 2026-11-20

클래스 다이어그램과 시퀀스 다이어그램에 나온 메서드 중 핵심 6개의 명세다. 메서드 이름과 타입은 클래스 다이어그램과 같다.

## F1. FreeTimeFinder.findFreeTime

| 항목 | 내용 |
| --- | --- |
| 시그니처 | `+findFreeTime(members: List<ShareData>, week: LocalDate, from: LocalTime, to: LocalTime): FreeTimeResult` |
| 소속 클래스 | FreeTimeFinder (service) |
| 관련 유스케이스 | 7. 공통 공강시간 찾기 |
| 설명 | 선택한 사람들의 수업과 시간이 정해진 일정을 30분 단위 격자에 겹쳐, 칸마다 바쁜 인원을 세고 만날 시간을 추천한다. |
| 입력 | `members`: 나와 선택한 친구들의 데이터(2명 이상) · `week`: 조회할 주의 월요일 · `from`, `to`: 조회 시간 범위(30분 단위) |
| 출력 | `FreeTimeResult`: 칸별 바쁜 인원 `busyCount[7][칸 수]`, 전체 인원, 모두 비는 구간 존재 여부 `allFree`, 추천 구간 목록 |
| 사전조건 | 친구를 1명 이상 골랐고 `from`이 `to`보다 이르다. (화면에서 먼저 검사, UC7 4-1, 5-1) |
| 사후조건 | 저장된 데이터는 바뀌지 않는다. |
| 예외 | `members`가 비었거나 `from >= to`이면 `IllegalArgumentException` |

**처리 절차**

1. 칸 수를 `(to - from) / 30분`으로 구하고, `busy[7][칸 수]`를 0으로 만든다.
2. 각 사람, 각 요일 `d`(0~6, 날짜 = `week + d`), 각 칸 `s`(시각 `t = from + 30분 × s`)에 대해 그 사람이 바쁜지 확인한다.
   1. 수업: 요일이 같고 `start <= t < end`이면 바쁘다.
   2. 일정: 날짜가 같고 `isTimed()`가 참이며 `start <= t < end`이면 바쁘다. 마감 시각만 있는 과제는 제외한다.
   3. 바쁘면 `busy[d][s]`를 1 올린다. 한 사람은 한 칸에 한 번만 센다.
3. 값이 0인 칸이 2칸(1시간) 이상 이어지는 구간을 찾아 추천 목록에 넣고 `allFree = true`로 한다.
4. 그런 구간이 없으면 격자의 최솟값 `m`을 찾는다. 값이 `m`인 칸이 이어지는 구간을 바쁜 인원 `m`과 함께 추천하고 `allFree = false`로 한다. (UC7 8-1)
5. 추천 구간을 날짜와 시각 순으로 정렬해 최대 5개를 돌려준다.

예: 3명을 09:00~22:00으로 조회하면 하루 26칸, 일주일 182칸을 계산한다. 수요일 15:00~17:00에 아무도 바쁘지 않으면 "수 15:00–17:00"이 추천된다.

## F2. ShareController.exportSchedule

| 항목 | 내용 |
| --- | --- |
| 시그니처 | `+exportSchedule(name: String, includeTitle: boolean, file: File): boolean` |
| 소속 클래스 | ShareController (controller) |
| 관련 유스케이스 | 5. 일정 내보내기 |
| 설명 | 내 이름, 시간표, 일정, 양파 진행도, 내보낸 날짜를 하나의 JSON 파일로 저장해 친구에게 전달할 수 있게 한다. |
| 입력 | `name`: 표시할 이름(1~10자) · `includeTitle`: 일정 제목 포함 여부 · `file`: 사용자가 고른 저장 위치 |
| 출력 | 저장에 성공하면 `true`, 실패하면 `false` |
| 사전조건 | 등록된 수업이나 일정이 1개 이상 있다. (UC5 1-1) |
| 사후조건 | `file` 위치에 JSON 파일이 생긴다. 내 원본 일정은 바뀌지 않는다. |
| 예외 | 이름이 비었거나 10자를 넘으면 `false` (UC5 3-1) · 쓰기 권한이 없는 등 저장에 실패하면 `false` (UC5 8-1) |

**처리 절차**

1. `name`의 앞뒤 공백을 지우고 길이가 1~10자인지 확인한다.
2. `scheduleBook.getAll()`로 내 수업과 일정을 가져온다.
3. `calculator.calculate(schedules, today)`로 진행도를 구한다(F6). 세는 일정이 없으면(`hasData()`가 거짓) 진행도를 `null`로 저장한다.
4. `buildMyShareData(name, includeTitle)`로 내보낼 데이터를 만든다. 원본이 아닌 **복사본**을 만든다.
   1. `includeTitle`이 거짓이면 복사본 일정의 제목을 "일정 있음"으로 바꾸고 날짜와 시간만 남긴다. (UC5 3-2)
   2. `exportedAt`에 오늘 날짜를, `titleIncluded`에 `includeTitle`을 넣는다.
5. `fileManager.write(file, shareData)`로 파일을 쓴다.
6. 쓰기 결과를 그대로 돌려준다. 화면은 `true`면 완료 메시지를, `false`면 오류 메시지를 출력한다.

파일 형식 예시:

```json
{
  "name": "홍길동",
  "exportedAt": "2026-11-20",
  "progress": 72,
  "titleIncluded": false,
  "lectures": [{"id": "L1", "subject": "자료구조", "day": "MONDAY", "start": "09:00", "end": "10:30", "room": "7-211"}],
  "schedules": [{"id": "S1", "type": "PERSONAL", "title": "일정 있음", "date": "2026-11-21", "start": "19:00", "end": "21:00", "done": false}]
}
```

## F3. ShareFileManager.read

| 항목 | 내용 |
| --- | --- |
| 시그니처 | `+read(file: File): ShareData` |
| 소속 클래스 | ShareFileManager |
| 관련 유스케이스 | 6. 친구 일정 가져오기 |
| 설명 | 친구에게 받은 JSON 파일을 읽고 형식과 필수 항목을 검증한 뒤 `ShareData`로 바꿔 돌려준다. |
| 입력 | `file`: 사용자가 고른 JSON 파일 |
| 출력 | 검증을 통과한 `ShareData` |
| 사전조건 | 사용자가 파일 선택 창에서 파일을 골랐다. |
| 사후조건 | 파일과 친구 목록은 바뀌지 않는다. 친구 목록 추가는 `ShareController.importFriend`가 맡는다. |
| 예외 | 파일을 읽지 못하면 `IOException` · JSON 형식이 아니거나 필수 항목이 빠지면 `InvalidShareFileException` (UC6 4-1) |

**처리 절차**

1. 파일 내용을 UTF-8 문자열로 읽는다.
2. util의 JSON 파서로 문자열을 파싱한다. 파싱에 실패하면 `InvalidShareFileException`을 던진다.
3. `validate`로 항목을 검사한다. 하나라도 틀리면 어느 항목이 틀렸는지 담아 예외를 던진다.
   1. `name`: 1~10자 문자열
   2. `exportedAt`: `yyyy-MM-dd` 날짜
   3. `progress`: 0~100 정수 또는 null(일정 없음)
   4. `titleIncluded`: 참/거짓
   5. `lectures`: 배열. 각 항목의 `day`가 요일 값이고 `start < end`
   6. `schedules`: 배열. 각 항목의 `type`이 ASSIGNMENT·EXAM·STUDY·PERSONAL 중 하나이고 `date`가 날짜 형식
4. 검사를 통과하면 `Lecture`, `Schedule` 객체를 만들어 `ShareData`에 담아 돌려준다.

호출하는 `importFriend`는 두 예외를 모두 잡아 `false`를 돌려주고, 화면은 "올바른 일정 파일이 아닙니다" 메시지를 출력한다.

## F4. ScheduleController.addSchedule

| 항목 | 내용 |
| --- | --- |
| 시그니처 | `+addSchedule(schedule: Schedule): boolean` |
| 소속 클래스 | ScheduleController (controller) |
| 관련 유스케이스 | 3. 일정 등록 (2-1, 과제·개인 일정) · 7. 공통 공강시간 찾기 (8-2) |
| 설명 | 과제나 개인 일정을 검증해 캘린더에 추가하고 파일에 저장한다. 시험은 공부 일정을 함께 만들어야 해서 `addExam`이 따로 맡는다. |
| 입력 | `schedule`: 일정 등록 창에서 만든 일정. 과제는 `end`에 마감 시각만 넣고 `start`는 비운다. |
| 출력 | 저장하면 `true`, 검증에 실패하면 `false` |
| 사전조건 | `schedule.type`이 ASSIGNMENT 또는 PERSONAL이다. |
| 사후조건 | 일정이 ScheduleBook에 추가되고 파일에 저장된다. 화면은 `CalendarPanel.refresh()`로 다시 그린다. `false`면 아무것도 저장하지 않는다. |
| 예외 | 유형이 EXAM이나 STUDY이면 `IllegalArgumentException` |

**처리 절차**

1. 유형을 확인한다. EXAM이나 STUDY면 예외를 던진다.
2. 입력값을 검증한다. 하나라도 틀리면 오류 메시지를 `lastError`에 남기고 `false`를 돌려준다.
   1. 제목이 비어 있지 않다.
   2. 날짜가 있다.
   3. 과제는 마감 시각(`end`)이 있다.
   4. 개인 일정에 시간을 넣었다면 `start < end`이다.
3. `id`가 없으면 UUID로 새 ID를 만든다. 친구 파일을 가져올 때 중복을 가려내는 데 쓴다.
4. `done`을 거짓으로 두고 `book.add(schedule)`로 추가한다.
5. `repository.save(book)`로 파일에 저장한다.
6. `true`를 돌려준다.

## F5. ScheduleController.addLecture

| 항목 | 내용 |
| --- | --- |
| 시그니처 | `+addLecture(lecture: Lecture): boolean` |
| 소속 클래스 | ScheduleController (controller) |
| 관련 유스케이스 | 2. 시간표 등록 |
| 설명 | 매주 반복되는 수업을 검증하고, 이미 등록된 수업과 겹치지 않으면 시간표에 추가한다. |
| 입력 | `lecture`: 과목명, 요일, 시작·종료 시간(30분 단위), 강의실(선택) |
| 출력 | 저장하면 `true`, 검증에 실패하거나 겹치면 `false` |
| 사전조건 | 시간표 화면의 수업 입력 창에서 [저장]을 눌렀다. |
| 사후조건 | 수업이 ScheduleBook에 추가되고 파일에 저장된다. `false`면 아무것도 저장하지 않는다. |
| 예외 | 없음. 실패는 모두 `false`로 알리고, 메시지는 `getLastError()`로 가져간다. (UC2 4-1, 4-2) |

**처리 절차**

1. `validate(lecture)`로 입력값을 검사한다. 틀리면 `false`를 돌려준다.
   1. 과목명이 비어 있지 않다.
   2. `start < end`이다.
   3. 시작과 종료의 분이 0 또는 30이다.
2. `book.findOverlap(lecture)`로 겹치는 수업을 찾는다. 같은 요일이고 `a.start < b.end` 그리고 `b.start < a.end`이면 겹친다고 본다. 그래서 10:30에 끝나는 수업 뒤에 10:30에 시작하는 수업은 겹치지 않는다.
3. 겹치는 수업이 있으면 그 수업 이름을 오류 메시지(`lastError`)로 남기고 `false`를 돌려준다.
4. `id`가 없으면 UUID로 새 ID를 만든다.
5. `book.addLecture(lecture)`로 추가하고 `repository.save(book)`로 저장한다.
6. `true`를 돌려준다.

수업 수정(`updateLecture`)도 같은 절차를 따른다. 단, 2단계에서 자기 자신(같은 `id`)은 겹침 검사에서 뺀다.

## F6. ProgressCalculator.calculate

| 항목 | 내용 |
| --- | --- |
| 시그니처 | `+calculate(schedules: List<Schedule>, today: LocalDate): Progress` |
| 소속 클래스 | ProgressCalculator (service) |
| 관련 유스케이스 | 11. 양파 상태 확인 · 5. 일정 내보내기 |
| 설명 | 최근 14일 동안의 과제·공부 일정 중 완료한 비율로 진행도를 계산한다. 양파 단계와 그림은 이 결과를 받은 Onion이 정한다. |
| 입력 | `schedules`: 내 전체 일정 · `today`: 오늘 날짜 |
| 출력 | `Progress`: 세는 일정 수 `total`, 완료 수 `done`, 진행도 `percent`(0~100), 밀린 일정 `overdue`, 데이터 존재 여부 `hasData()` |
| 사전조건 | 없음 |
| 사후조건 | 저장된 일정은 바뀌지 않는다. |
| 예외 | 없음. 일정이 없으면 `hasData()`가 거짓인 결과를 돌려준다. |

**처리 절차**

1. 기간 시작일을 `today - 13일`로 정한다. 오늘을 포함해 14일이다.
2. `isCounted`로 각 일정을 셀지 정한다. 유형이 ASSIGNMENT 또는 STUDY이고, 아래 중 하나이면 센다.
   1. 날짜가 시작일부터 어제 사이다. 완료 여부와 상관없이 센다.
   2. 날짜가 오늘이고 이미 완료했다. 아직 안 한 오늘 일정은 세지 않아서, 아침부터 양파가 시들지 않는다.
3. `total`은 센 일정 수, `done`은 그중 완료한 수다.
4. 센 일정 중 미완료인 것을 날짜가 오래된 순으로 `overdue`에 넣는다. 양파 상세 창의 밀린 일정 목록이 이것을 쓴다.
5. `total`이 0이면 `percent = 0`, `hasData()`는 거짓으로 돌려준다. 양파는 기본 양파를 보여준다. (UC11 1-1)
6. 아니면 `percent = Math.round(done × 100.0 / total)`로 계산한다. `100.0`을 써서 자바 정수 나눗셈으로 결과가 0이 되는 문제를 막는다.

일정이 1~2개뿐이어도 그대로 계산한다. 첫 일정을 놓치면 바로 썩은 양파가 나오는데, 초반부터 경각심을 주려는 의도다.

예: 최근 14일 동안 센 일정 10개 중 7개를 완료했으면 70%이고, 시든 양파(50% 이상 80% 미만)가 나온다.

## 팀 확인 필요 사항

- 과제 마감 시각 저장 위치: 과제는 `end`에 마감 시각만 넣는다. 승민의 강제 알림(`ScheduleBook.findDueTomorrow`)도 이 필드를 읽는다.
- 내보내기에서 수업명: UC5 3-2는 일정 제목만 "일정 있음"으로 바꾼다. 시간표의 과목명도 숨길지 정해야 한다.
- 예외 클래스: F3의 `InvalidShareFileException`을 클래스 다이어그램에 넣을지 정해야 한다.
- 진행도 없음 표현: 내보낸 파일에는 `progress: null`로 저장하고, `Onion.getStage(null)`은 기본 양파를 돌려주도록 승민과 맞춘다.
