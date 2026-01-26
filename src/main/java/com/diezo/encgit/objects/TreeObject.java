package com.diezo.encgit.objects;

public class TreeObject extends GitObject {

    public TreeObject(byte[] content) {
        super("tree", content);
    }

    public static void catTreeObject(byte[] content) {
        // TODO: Implement method
        throw new UnsupportedOperationException("Not supported yet!");
    }
}
