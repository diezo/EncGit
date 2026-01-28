package com.diezo.encgit.objects;

import java.util.List;

public class FilesystemDirectory extends FilesystemEntry {
    public List<FilesystemEntry> entries;

    public FilesystemDirectory(String title, List<FilesystemEntry> entries) {
        this.title = title;
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "Directory(title=\"" + title + "\", entries=" + entries + ")";
    }
}
