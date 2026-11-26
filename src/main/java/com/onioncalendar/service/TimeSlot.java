package com.onioncalendar.service;

import java.time.LocalDateTime;

/** 추천 시간 구간. busy는 그 구간에 바쁜 인원 수(0이면 모두 가능). */
public record TimeSlot(LocalDateTime start, LocalDateTime end, int busy) { }
