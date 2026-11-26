package com.onioncalendar.service;

import java.time.LocalDate;

/** 시험 등록 창의 공부량 자동 할당 입력값. startDate는 기간 설정(3일/2주/3주/직접)에서 계산한 공부 시작일. */
public record StudyGoal(int readings, int solvings, LocalDate startDate) { }
