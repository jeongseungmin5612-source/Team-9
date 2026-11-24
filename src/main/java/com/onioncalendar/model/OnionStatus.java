package com.onioncalendar.model;

/**
 * 양파 상태 (ScheduleController.getOnionStatus 결과).
 * progress가 Onion.NO_DATA(-1)이면 셀 일정이 없다는 뜻이고 stage는 DEFAULT.
 */
public record OnionStatus(int progress, OnionStage stage, int doneCount) { }
