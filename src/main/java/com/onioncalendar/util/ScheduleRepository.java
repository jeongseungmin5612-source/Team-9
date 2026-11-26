package com.onioncalendar.util;

import com.onioncalendar.model.Lecture;
import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleBook;
import com.onioncalendar.model.ScheduleType;
import com.onioncalendar.model.TimeBlock;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 내 수업과 일정을 JSON 파일로 저장하고 불러온다. */
public class ScheduleRepository {
    private final Path path;

    public ScheduleRepository(Path path) { this.path = path; }

    /** 파일이 없으면 빈 ScheduleBook */
    public ScheduleBook load() {
        ScheduleBook book = new ScheduleBook();
        if (!Files.exists(path)) return book;
        try {
            Map<String, Object> root = asMap(Json.parse(Files.readString(path, StandardCharsets.UTF_8)));
            for (Object o : asList(root.get("lectures"))) book.add(lectureFrom(asMap(o)));
            for (Object o : asList(root.get("schedules"))) book.add(scheduleFrom(asMap(o)));
            return book;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void save(ScheduleBook book) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("lectures", lecturesToJson(book.getLectures()));
        root.put("schedules", schedulesToJson(book.getSchedules()));
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, Json.write(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---- JSON 변환 (ShareFileManager에서도 같이 씀) ----

    public static List<Object> lecturesToJson(List<Lecture> lectures) {
        List<Object> out = new ArrayList<>();
        for (Lecture l : lectures) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("subject", l.getSubject());
            m.put("day", l.getDay().name());
            m.put("start", l.getStart().toString());
            m.put("end", l.getEnd().toString());
            m.put("room", l.getRoom());
            out.add(m);
        }
        return out;
    }

    public static List<Object> schedulesToJson(List<? extends TimeBlock> schedules) {
        List<Object> out = new ArrayList<>();
        for (TimeBlock b : schedules) {
            Schedule s = (Schedule) b;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("type", s.getType().name());
            m.put("title", s.getTitle());
            m.put("start", s.getStart() == null ? null : s.getStart().toString());
            m.put("end", s.getEnd() == null ? null : s.getEnd().toString());
            m.put("done", s.isDone());
            m.put("examId", s.getExamId());
            out.add(m);
        }
        return out;
    }

    public static Lecture lectureFrom(Map<String, Object> m) {
        return new Lecture((String) m.get("subject"),
                DayOfWeek.valueOf((String) m.get("day")),
                LocalTime.parse((String) m.get("start")),
                LocalTime.parse((String) m.get("end")),
                (String) m.get("room"));
    }

    public static Schedule scheduleFrom(Map<String, Object> m) {
        String start = (String) m.get("start"), end = (String) m.get("end");
        return new Schedule((String) m.get("id"),
                ScheduleType.valueOf((String) m.get("type")),
                (String) m.get("title"),
                start == null ? null : LocalDateTime.parse(start),
                end == null ? null : LocalDateTime.parse(end),
                Boolean.TRUE.equals(m.get("done")),
                (String) m.get("examId"));
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object o) {
        if (!(o instanceof Map)) throw new IllegalArgumentException("객체가 아님");
        return (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    static List<Object> asList(Object o) {
        if (o == null) return new ArrayList<>();
        if (!(o instanceof List)) throw new IllegalArgumentException("배열이 아님");
        return (List<Object>) o;
    }
}
