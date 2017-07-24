//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package model;

import java.util.ArrayList;
import java.util.List;

public class Tag {
    int id;
    List<Integer> users;

    public Tag(int id) {
        this.id = id;
        this.users = new ArrayList<>();
    }

    public void tagaMovie(int userId) {
        this.users.add(userId);
    }

    public int getId() {
        return this.id;
    }

    public List<Integer> getUsers() {
        return this.users;
    }
}
