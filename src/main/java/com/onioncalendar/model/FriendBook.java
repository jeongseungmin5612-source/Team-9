package com.onioncalendar.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 가져온 친구 목록 (이름으로 구분). */
public class FriendBook {
    private final Map<String, Friend> friends = new LinkedHashMap<>();

    /** 같은 이름이 있으면 바꾼다 */
    public void put(Friend f) { friends.put(f.getName(), f); }

    public Friend findByName(String name) { return friends.get(name); }

    public void remove(Friend f) { friends.remove(f.getName()); }

    public List<Friend> getAll() { return new ArrayList<>(friends.values()); }
}
