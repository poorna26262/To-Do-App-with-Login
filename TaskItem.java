package com.example.todologin;

public class TaskItem {
    public final long id;
    public final String title;
    public final String notes;
    public final boolean completed;

    public TaskItem(long id, String title, String notes, boolean completed) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.completed = completed;
    }
}
