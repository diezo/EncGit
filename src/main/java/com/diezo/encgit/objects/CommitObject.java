package com.diezo.encgit.objects;

public class CommitObject extends GitObject {

    public CommitObject(byte[] content) {
        super("commit", content);
    }

    public static void catCommitObject(byte[] content) {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not supported yet!");
    }
}
