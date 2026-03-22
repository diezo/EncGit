## EncGit - An Encrypted VCS Written in Java

[![wakatime](https://wakatime.com/badge/user/018dbb56-f37d-40a3-96e0-e01ce5e8b6ac/project/600325b2-a3e1-4230-b831-f182327303ed.svg)](https://wakatime.com/badge/user/018dbb56-f37d-40a3-96e0-e01ce5e8b6ac/project/600325b2-a3e1-4230-b831-f182327303ed)

![](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white)

[![LinkedIn](https://custom-icon-badges.demolab.com/badge/LinkedIn-0A66C2?logo=linkedin-white&logoColor=fff)](https://linkedin.com/in/deepaksonii)
[![GitHub](https://img.shields.io/badge/GitHub-%23121011.svg?logo=github&logoColor=white)](https://github.com/diezo)

An encrypted version control system written in Java

> [!NOTE]
> This project is under active experimentation, and not yet suitable for production use.

## 📦 Features
- ✅ SHA-256 Object Hashing
- ✅ `zlib deflate` Object Compression
- ✅ Staging Area
- ✅ Commits
- ✅ AES-256 Encrypted Blob Storage
- Branching
- Diff Engine
- Garbage Collector (Explicit + Implicit)
- Unit Tests Integration
- Object Integrity Checks
- Configuration Support

## 🔑 AES-256 Object Encryption
EncGit encrypts all object data at rest using AES-256.

During `encgit init`, a cryptographically secure 256-bit key is generated and stored in the user's home directory (outside the repository). The repository maintains only a reference to this key via `.encgit/ref.key`.

All subsequent operations use this key to perform transparent encryption and decryption of object data.

Key reference format (`.encgit/ref.key`):

```
ref: C:\Users\jake\.encgit-keys\9e0bd17bfb0c5e53ed19acee5969486c.key
```

> [!WARNING]
> Loss of the key file results in permanent data loss, as encrypted objects cannot be recovered without it.

## ⚒️ Porcelain Commands (High-level)
Here's the list of supported user-friendly commands:

<details>

<summary><b>encgit init</b></summary>

Initializes directory as an empty EncGit repository

```
encgit init
```

</details>

<details>

<summary><b>encgit add</b></summary>

Adds files to the staging area

```
encgit add <file1> <file2> ...
```

</details>

<details>

<summary><b>encgit commit</b></summary>

Creates a commit from the current staging area

```
encgit commit -m <message>
```

</details>

## ⚒️ Plumbing Commands (Low-level)
Here's the list of supported internal commands:

<details>

<summary><b>encgit cat-file</b></summary>

Pretty-prints details of the specified object

```
encgit cat-file <flag> <object-hash>
```

Flags:
- ```-t``` prints object type
- ```-p``` prints object content

</details>
