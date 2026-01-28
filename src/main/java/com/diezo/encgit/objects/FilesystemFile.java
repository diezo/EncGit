package com.diezo.encgit.objects;

public class FilesystemFile extends FilesystemEntry {
    public String objectHash;

    public FilesystemFile(String filename, String objectHash) {
        this.title = filename;
        this.objectHash = objectHash;
    }

    @Override
    public String toString() {
        return "File(title=\"" + title + "\", objectHash=" + (objectHash != null ? ("\"" + objectHash + "\"") : "null") + ")";
    }
}
