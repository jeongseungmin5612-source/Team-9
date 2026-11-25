package com.onioncalendar.util;

import com.onioncalendar.model.Friend;
import com.onioncalendar.model.Lecture;
import com.onioncalendar.model.Onion;
import com.onioncalendar.model.Schedule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 공유용 JSON 파일 읽기·쓰기와 형식 검증. */
public class ShareFileManager {
    /** 파일 형식 버전 (나중에 형식을 바꿀 때 구분용) */
    public static final int FORMAT_VERSION = 1;

    public void write(Path path, Friend f) throws IOException {
        Files.writeString(path, Json.write(toJson(f)), StandardCharsets.UTF_8);
    }

    /** 파일을 읽어 검증한다. 형식이 틀리면 IllegalArgumentException(이유 포함), 못 읽으면 IOException. */
    public Friend read(Path path) throws IOException {
        Object parsed;
        try {
            parsed = Json.parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JSON 형식이 아닙니다.");
        }
        return fromJson(parsed);
    }

    static Map<String, Object> toJson(Friend f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("app", "OnionCalendar");
        m.put("version", FORMAT_VERSION);
        m.put("name", f.getName());
        m.put("exportedAt", f.getExportedAt().toString());
        m.put("progress", f.getProgress() == Onion.NO_DATA ? null : f.getProgress());
        m.put("lectures", ScheduleRepository.lecturesToJson(f.getLectures()));
        m.put("schedules", ScheduleRepository.schedulesToJson(f.getSchedules()));
        return m;
    }

    static Friend fromJson(Object o) {
        if (!(o instanceof Map)) throw new IllegalArgumentException("일정 파일 형식이 아닙니다.");
        Map<String, Object> m = ScheduleRepository.asMap(o);
        Object name = m.get("name");
        if (!(name instanceof String) || ((String) name).isBlank() || ((String) name).length() > 10) {
            throw new IllegalArgumentException("이름(name)이 없거나 10자를 넘습니다.");
        }
        LocalDate exportedAt;
        try {
            exportedAt = LocalDate.parse(String.valueOf(m.get("exportedAt")));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("내보낸 날짜(exportedAt) 형식이 틀립니다.");
        }
        Object p = m.get("progress");
        int progress;
        if (p == null) progress = Onion.NO_DATA;
        else if (p instanceof Double && (Double) p >= 0 && (Double) p <= 100) progress = (int) Math.round((Double) p);
        else throw new IllegalArgumentException("진행도(progress)는 0~100 또는 null이어야 합니다.");

        List<Lecture> lectures = new ArrayList<>();
        List<Schedule> schedules = new ArrayList<>();
        try {
            for (Object x : ScheduleRepository.asList(m.get("lectures"))) {
                Lecture l = ScheduleRepository.lectureFrom(ScheduleRepository.asMap(x));
                if (!l.getStart().isBefore(l.getEnd())) throw new IllegalArgumentException();
                lectures.add(l);
            }
            for (Object x : ScheduleRepository.asList(m.get("schedules"))) {
                Schedule s = ScheduleRepository.scheduleFrom(ScheduleRepository.asMap(x));
                if (s.getEnd() == null) throw new IllegalArgumentException();
                schedules.add(s);
            }
        } catch (RuntimeException e) {   // 형식 오류, 잘못된 요일·유형·시각 등
            throw new IllegalArgumentException("수업 또는 일정 항목의 형식이 틀립니다.");
        }
        return new Friend((String) name, progress, exportedAt, lectures, schedules);
    }
}
