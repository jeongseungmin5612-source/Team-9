# 설계 산출물: 캘린더 · 일정 기록 · 일정 공유 파트 (이로운)

중간 보고서(CP2)에 들어갈 이로운 담당 파트의 설계 산출물이다. 코드 구현 전 설계 단계 자료다.

## 폴더 구성

| 경로 | 내용 |
| --- | --- |
| `class-diagram/class_logic.png` | 클래스 다이어그램 (model · controller · service · repository) |
| `class-diagram/class_view.png` | 클래스 다이어그램 (Swing 화면 클래스와 컨트롤러 관계) |
| `class-diagram/*.mmd` | 위 다이어그램의 Mermaid 원본. 팀 전체 다이어그램으로 합칠 때 이 텍스트를 쓰면 정확하다 |
| `function-spec/function_spec.md` | 함수 명세 6개 (F1~F6) |
| `gui/*.png` | GUI 초안 화면 10개. 파일 이름이 화면 클래스 이름이다 |

## 다른 파트와 맞닿는 곳

- **Onion (승민)**: 진행도 계산은 일정 파트의 `ProgressCalculator`가 하고, 양파 파트는 `Onion.getStage(percent)`로 단계와 그림만 정한다. 일정이 없으면 percent는 `null`.
- **StudyPlanner (현국)**: 시험 등록 시 `ScheduleController.addExam(exam, goal)`이 `StudyPlanner.allocate(exam, goal)`을 호출한다 (보고서 UC3 유지).
- **강제 알림 (승민)**: `ScheduleBook.findDueTomorrow(today)`로 마감 하루 전 과제를 가져간다.
- 다이어그램에서 «다른 파트» 표시가 붙은 칸은 합칠 때 각 파트의 실제 클래스로 바꾸면 된다.
- 관계선은 연관(→)과 합성(◆)만 썼다.

## GUI 초안

화면 10개의 이미지는 `gui/` 폴더에 있다. 원본 캔버스: https://claude.ai/artifact/BAApUmEceN3wVxLFviuTp7

| 줄 | 화면 (클래스) |
| --- | --- |
| 캘린더 · 일정 (UC1, 3, 4) | CalendarPanel, DayDetailDialog, ScheduleDetailDialog, ScheduleFormDialog |
| 시간표 (UC2) | TimetablePanel, LectureFormDialog |
| 공유 (UC5, 6, 8) | SharePanel, ExportDialog, FriendInfoDialog |
| 공통 공강시간 (UC7) | FreeTimePanel |

## 추후 변경 예정

- 시퀀스 다이어그램 "일정 내보내기" 9~10번: `Onion.calcProgress` → `ProgressCalculator.calculate`
- 시퀀스 다이어그램 "양파 상태 확인": 진행도 계산 위치 변경 반영 (승민)
- 보고서 UC11: 캘린더 안 양파 영역 → 바탕화면 상주 양파 (승민)

보고서와의 차이 전체 목록: https://claude.ai/artifact/BgTMD5YDUbqCGM8ArFCaNv
