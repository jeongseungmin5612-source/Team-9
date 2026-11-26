package com.onioncalendar.util;

import com.onioncalendar.model.Friend;
import com.onioncalendar.model.FriendBook;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 가져온 친구 목록을 data/friends.json에 저장하고 불러온다. */
public class FriendRepository {
    private final Path path;

    public FriendRepository(Path path) { this.path = path; }

    public FriendBook load() {
        FriendBook book = new FriendBook();
        if (!Files.exists(path)) return book;
        try {
            Map<String, Object> root = ScheduleRepository.asMap(Json.parse(Files.readString(path, StandardCharsets.UTF_8)));
            for (Object o : ScheduleRepository.asList(root.get("friends"))) book.put(ShareFileManager.fromJson(o));
            return book;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void save(FriendBook book) {
        List<Object> list = new ArrayList<>();
        for (Friend f : book.getAll()) list.add(ShareFileManager.toJson(f));
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("friends", list);
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, Json.write(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
