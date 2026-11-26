package com.onioncalendar.controller;

import com.onioncalendar.model.Friend;
import com.onioncalendar.model.FriendBook;
import com.onioncalendar.model.Lecture;
import com.onioncalendar.model.Onion;
import com.onioncalendar.model.Schedule;
import com.onioncalendar.model.ScheduleBook;
import com.onioncalendar.model.TimeBlock;
import com.onioncalendar.service.FreeTimeFinder;
import com.onioncalendar.service.FreeTimeResult;
import com.onioncalendar.util.FriendRepository;
import com.onioncalendar.util.ShareFileManager;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** 일정 공유(내보내기·가져오기), 친구 목록, 공통 공강시간 찾기 (UC5~UC8). */
public class ShareController {
    /** 제목 미포함으로 내보낼 때 쓰는 이름 */
    public static final String HIDDEN_SCHEDULE = "일정 있음";
    public static final String HIDDEN_LECTURE = "수업";

    private final ScheduleBook scheduleBook;
    private final FriendBook friendBook;
    private final ShareFileManager fileManager;
    private final FriendRepository friendRepository;
    private final FreeTimeFinder finder;
    /** 같은 이름의 친구를 덮어쓸지 묻는 쪽 (SharePanel.confirmReplace) */
    private Predicate<String> replaceConfirmer = name -> true;
    private String lastError = "";

    public ShareController(ScheduleBook scheduleBook, FriendBook friendBook, ShareFileManager fileManager,
                           FriendRepository friendRepository, FreeTimeFinder finder) {
        this.scheduleBook = scheduleBook;
        this.friendBook = friendBook;
        this.fileManager = fileManager;
        this.friendRepository = friendRepository;
        this.finder = finder;
    }

    public void setReplaceConfirmer(Predicate<String> confirmer) { this.replaceConfirmer = confirmer; }

    /** 내 수업·일정·진행도를 파일로 저장한다. 원본 일정은 바꾸지 않는다. */
    public boolean exportSchedule(String name, boolean includeTitle, Path path) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty() || n.length() > 10) { lastError = "이름은 1~10자로 입력하세요."; return false; }
        if (scheduleBook.getAll().isEmpty()) { lastError = "내보낼 수업이나 일정이 없습니다."; return false; }

        List<Lecture> lectures = new ArrayList<>();
        for (Lecture l : scheduleBook.getLectures()) {
            lectures.add(includeTitle ? l
                    : new Lecture(HIDDEN_LECTURE, l.getDay(), l.getStart(), l.getEnd(), null));
        }
        List<Schedule> schedules = new ArrayList<>();
        for (Schedule s : scheduleBook.getSchedules()) {
            Schedule c = s.copy();
            if (!includeTitle) c.setTitle(HIDDEN_SCHEDULE);
            schedules.add(c);
        }
        int progress = Onion.calcProgress(scheduleBook.getSchedules(), LocalDate.now());
        Friend me = new Friend(n, progress, LocalDate.now(), lectures, schedules);
        try {
            fileManager.write(path, me);
            return true;
        } catch (IOException e) {
            lastError = "파일을 저장하지 못했습니다: " + e.getMessage();
            return false;
        }
    }

    /** 친구 파일을 가져온다. 같은 이름이 있으면 replaceConfirmer에게 묻는다. */
    public boolean importFriend(Path path) {
        Friend f;
        try {
            f = fileManager.read(path);
        } catch (IOException e) {
            lastError = "파일을 읽지 못했습니다.";
            return false;
        } catch (IllegalArgumentException e) {
            lastError = "올바른 일정 파일이 아닙니다. (" + e.getMessage() + ")";
            return false;
        }
        if (friendBook.findByName(f.getName()) != null && !replaceConfirmer.test(f.getName())) {
            lastError = "";   // 사용자가 취소함
            return false;
        }
        friendBook.put(f);
        friendRepository.save(friendBook);
        return true;
    }

    /** 나 + 고른 친구들의 공통 공강시간 */
    public FreeTimeResult findCommonFreeTime(List<Friend> friends, LocalDate week, LocalTime from, LocalTime to) {
        List<List<TimeBlock>> blocks = new ArrayList<>();
        blocks.add(scheduleBook.getAll());
        for (Friend f : friends) blocks.add(f.getBlocks());
        return finder.findFreeTime(blocks, week, from, to);
    }

    public List<Friend> getFriends() { return friendBook.getAll(); }

    public void deleteFriend(Friend f) {
        friendBook.remove(f);
        friendRepository.save(friendBook);
    }

    public String getLastError() { return lastError; }
}
