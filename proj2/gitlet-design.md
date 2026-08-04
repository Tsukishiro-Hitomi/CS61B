# Gitlet Design Document

**Name**:

## Classes and Data Structures

### `Main`

The command line entry point for Gitlet. `Main` is intentionally thin: it
validates the command name and operand count, checks whether the repository has
been initialized when required, and delegates all repository behavior to
`Repository`.

#### Fields

`Main` does not need persistent fields.

#### Responsibilities

1. Print `Please enter a command.` when no command is supplied.
2. Print `No command with that name exists.` for an unknown command.
3. Print `Incorrect operands.` when a known command has the wrong operand
   pattern.
4. For all commands except `init`, print `Not in an initialized Gitlet
   directory.` if `.gitlet` does not exist in the current working directory.
5. Dispatch valid commands to static methods in `Repository`.


### `Repository`

The main service class for the Gitlet repository. It owns all filesystem paths,
loads and saves persistent state, and implements the behavior of every command.
The class can be mostly static because every invocation of `java gitlet.Main`
is a new process and must reload repository state from disk.

#### Fields

1. `static final File CWD`

   The current working directory where user files live.

2. `static final File GITLET_DIR`

   The `.gitlet` directory under `CWD`.

3. `static final File COMMITS_DIR`

   Directory containing serialized `Commit` objects. Each file is named by the
   commit SHA-1 id.

4. `static final File BLOBS_DIR`

   Directory containing raw file contents. Each file is named by the blob SHA-1
   id.

5. `static final File REFS_DIR`

   Directory for references. The only required child directory is
   `refs/heads`.

6. `static final File HEADS_DIR`

   Directory containing one file per branch. The filename is the branch name,
   and the file contents are the commit id at the branch head.

7. `static final File HEAD_FILE`

   File containing the current branch name, for example `master`.

8. `static final File INDEX_FILE`

   Serialized `StagingArea` object representing the pending additions and
   removals.

9. `static final File REMOTES_DIR`

   Directory containing remote metadata. Each file is named by a remote name
   and stores the path to that remote repository's `.gitlet` directory.

#### Helper Methods

1. `isInitialized()`

   Returns whether `.gitlet` exists and is a directory.

2. `readStage()` and `saveStage(StagingArea stage)`

   Load and persist the staging area from `INDEX_FILE`.

3. `currentBranchName()`, `currentBranchFile()`, `headCommitId()`, and
   `headCommit()`

   Read the active branch and its head commit.

4. `readCommit(String id)` and `saveCommit(Commit commit)`

   Load and persist commits by id.

5. `resolveCommitId(String prefix)`

   Converts a full or abbreviated commit id into the matching full id. If there
   is no match, the caller reports `No commit with that id exists.`

6. `writeBlob(File file)`

   Reads a working directory file, computes its SHA-1 from the file bytes, saves
   the bytes to `BLOBS_DIR` if needed, and returns the blob id.

7. `checkoutBlob(String blobId, String fileName)`

   Writes the blob contents to `CWD/fileName`.

8. `hasUntrackedConflict(Commit target)`

   Returns true if switching to `target` would overwrite a working directory
   file that is not tracked by the current head commit and is not staged for
   addition.

9. `replaceWorkingDirectoryWith(Commit target)`

   Restores every file tracked by `target`, deletes files tracked by the current
   head but absent from `target`, and leaves unrelated files alone. This helper
   is shared by branch checkout and reset.

10. `remotePath(String remoteName)`

    Reads `.gitlet/remotes/<remoteName>` and returns the configured remote
    `.gitlet` directory path. The caller reports `Remote directory not found.`
    if the path does not name an existing directory.

11. `remoteHeadsDir(File remoteGitletDir)`, `remoteCommitsDir(File
    remoteGitletDir)`, and `remoteBlobsDir(File remoteGitletDir)`

    Build the standard subdirectories for a remote repository. Remote
    repositories use the same internal layout as the local repository.

12. `copyReachableCommitsAndBlobs(String startCommitId, File sourceGitletDir,
    File destinationGitletDir)`

    Starting from a commit id, traverse parents and second parents. For each
    commit not already present in the destination, copy the serialized commit
    object and all referenced blobs from source to destination.

13. `isAncestor(String possibleAncestorId, String descendantId, File
    gitletDir)`

    Returns whether `possibleAncestorId` is reachable by following first and
    second parent links from `descendantId` inside the given repository.


### `Commit`

A snapshot of the repository at one point in time. Commits are immutable after
they are created. A commit stores references to blobs rather than storing file
contents directly.

#### Fields

1. `String message`

   The commit message.

2. `String timestamp`

   The formatted timestamp printed by `log`. The initial commit uses the Unix
   epoch. Other commits use the creation time.

3. `String parent`

   The first parent commit id. This is `null` for the initial commit.

4. `String secondParent`

   The second parent commit id for merge commits. This is `null` for normal
   commits.

5. `TreeMap<String, String> blobs`

   A deterministic mapping from file name to blob id for every file tracked by
   this commit.

6. `String id`

   The SHA-1 id computed from the commit's message, timestamp, parent ids, and
   tracked blob mapping.

#### Methods

1. Constructor for the initial commit.
2. Constructor for normal commits.
3. Constructor for merge commits.
4. Accessors for message, timestamp, parent ids, tracked files, and id.
5. `computeId()`, called only after all other commit fields have been set.


### `StagingArea`

The mutable index between the current head commit and the next commit. It is
serialized to disk after every command that changes it.

#### Fields

1. `TreeMap<String, String> additions`

   Files staged for addition or replacement. The key is the file name and the
   value is the blob id to track in the next commit.

2. `TreeSet<String> removals`

   Files staged for removal in the next commit.

#### Methods

1. `isEmpty()`
2. `clear()`
3. `stageAddition(String fileName, String blobId)`
4. `stageRemoval(String fileName)`
5. `unstageAddition(String fileName)`
6. `isStagedForAddition(String fileName)`
7. `isStagedForRemoval(String fileName)`


## Algorithms

### Command Dispatch

`Main.main` first checks whether `args.length == 0`. If not, it switches on
`args[0]`. Each case validates the exact operand pattern before calling
`Repository`. After validation, commands other than `init` check
`Repository.isInitialized()`.

### `init`

If `.gitlet` already exists, print `A Gitlet version-control system already
exists in the current directory.` and stop. Otherwise:

1. Create `.gitlet`, `commits`, `blobs`, and `refs/heads`.
2. Create the initial commit with message `initial commit`, timestamp at the
   Unix epoch, no parents, and an empty blob map.
3. Save the initial commit under `commits/<id>`.
4. Create branch file `refs/heads/master` containing the initial commit id.
5. Write `master` into `HEAD`.
6. Save an empty `StagingArea` to `index`.

### `add [file name]`

If the file does not exist in `CWD`, print `File does not exist.` and stop.
Otherwise:

1. Compute and save the blob for the working directory file.
2. Compare that blob id with the blob tracked by the head commit for the same
   file.
3. If they are the same, remove the file from staged additions.
4. Otherwise, stage the file for addition with the new blob id.
5. In all cases, remove the file from staged removals because the user is
   choosing to keep/add it.
6. Save the stage.

### `commit [message]`

If the message is empty, print `Please enter a commit message.` and stop. If
the staging area has no additions or removals, print `No changes added to the
commit.` and stop. Otherwise:

1. Copy the head commit's blob map.
2. Apply staged removals by deleting those file names from the copy.
3. Apply staged additions by inserting/replacing those file names with staged
   blob ids.
4. Create a new commit with the copied map, the message, the current head as
   first parent, and no second parent.
5. Save the commit.
6. Update the current branch file to the new commit id.
7. Clear and save the stage.

### `rm [file name]`

Load the stage and head commit. A file is removable if it is staged for
addition or tracked by the head commit.

1. If staged for addition, unstage it.
2. If tracked by the head commit, stage it for removal and delete it from the
   working directory using `restrictedDelete`.
3. If neither condition applies, print `No reason to remove the file.`
4. Save the stage.

### `log`

Start at the head commit. Print the commit, then repeatedly follow only the
first parent until reaching the initial commit. For merge commits, print a
`Merge:` line using the first seven characters of each parent id.

### `global-log`

Iterate over every plain file in `COMMITS_DIR`, read each commit, and print it.
The order is not important.

### `find [commit message]`

Scan all commits. Print the id of every commit whose message exactly matches
the operand. If none match, print `Found no commit with that message.`

### `status`

Print these sections in order:

1. `=== Branches ===`
2. `=== Staged Files ===`
3. `=== Removed Files ===`
4. `=== Modifications Not Staged For Commit ===`
5. `=== Untracked Files ===`

Branches, staged files, and removed files are printed in lexicographic order.
The current branch is prefixed with `*`. The last two sections can initially be
empty, but the headers and blank lines must still be printed.

### Checkout Forms

There are three checkout forms, selected in `Main` by operand pattern.

1. `checkout -- [file name]`

   Restore the file from the head commit. If the file is not tracked, print
   `File does not exist in that commit.`

2. `checkout [commit id] -- [file name]`

   Resolve the commit id, then restore the file from that commit. If the commit
   id cannot be resolved, print `No commit with that id exists.` If the file is
   not tracked by that commit, print `File does not exist in that commit.`

3. `checkout [branch name]`

   If the branch does not exist, print `No such branch exists.` If it is the
   current branch, print `No need to checkout the current branch.` If an
   untracked file would be overwritten, print `There is an untracked file in
   the way; delete it, or add and commit it first.` Otherwise use
   `replaceWorkingDirectoryWith` to restore the target branch head, update
   `HEAD`, and clear the staging area.

### `branch [branch name]`

If the branch already exists, print `A branch with that name already exists.`
Otherwise create `refs/heads/<branch name>` containing the current head commit
id.

### `rm-branch [branch name]`

If the branch does not exist, print `A branch with that name does not exist.`
If it is the current branch, print `Cannot remove the current branch.`
Otherwise delete its file from `HEADS_DIR`.

### `reset [commit id]`

Resolve the commit id. If it cannot be resolved, print `No commit with that id
exists.` If an untracked file would be overwritten, print `There is an
untracked file in the way; delete it, or add and commit it first.` Otherwise:

1. Restore the target commit with `replaceWorkingDirectoryWith`.
2. Move the current branch pointer to the target commit.
3. Clear and save the staging area.

### `merge [branch name]`

First perform failure checks in this order:

1. If the staging area is not empty, print `You have uncommitted changes.`
2. If the branch does not exist, print `A branch with that name does not
   exist.`
3. If the branch is the current branch, print `Cannot merge a branch with
   itself.`
4. If an untracked file would be overwritten, print `There is an untracked file
   in the way; delete it, or add and commit it first.`

Then compute the split point:

1. Traverse all ancestors of the current head, following both first and second
   parents, and record their distance from the current head.
2. Traverse all ancestors of the given branch head, following both first and
   second parents, and record their distance from the given head.
3. Among common ancestors, choose the one with the smallest combined distance
   from both heads. Ties can be broken consistently, for example by the current
   side distance and then commit id.

Handle special cases:

1. If the split point is the given branch head, print `Given branch is an
   ancestor of the current branch.` and stop.
2. If the split point is the current head, check out the given branch state,
   move the current branch pointer to the given head, clear the stage, print
   `Current branch fast-forwarded.`, and stop.

For the general case, collect the union of file names from the split, current,
and given commits. For each file, compare blob ids in the three commits:

1. If the file changed only in the given branch, check out the given version and
   stage it.
2. If the file was removed in the given branch and unchanged in the current
   branch, stage it for removal and delete it from the working directory.
3. If the file changed only in the current branch, leave it alone.
4. If both branches made the same change or both removed the file, leave it
   alone.
5. If the branches made conflicting changes, write conflict contents to the
   working directory and stage the file.

Conflict contents are produced by this exact concatenation:

```text
"<<<<<<< HEAD\n" + currentContents + "=======\n" + givenContents + ">>>>>>>\n"
```

Missing file contents are treated as empty strings. This formula deliberately
does not add an extra newline after `currentContents` or `givenContents`;
whether the separator appears on a new line depends on whether the original
file contents already ended with a newline.

After processing files, create a merge commit with message
`Merged [given branch name] into [current branch name].`, first parent equal to
the old current head, and second parent equal to the given branch head. If any
conflict occurred, print `Encountered a merge conflict.` after the merge commit
is created.

### `add-remote [remote name] [remote .gitlet directory]`

If a remote with the given name already exists, print `A remote with that name
already exists.` and stop. Otherwise:

1. Convert all `/` characters in the supplied path into `File.separator`.
2. Create `.gitlet/remotes` if it does not already exist.
3. Save the converted path in `.gitlet/remotes/<remote name>`.

The command does not validate whether the target path exists or whether it is a
real Gitlet repository. Those checks happen when `push`, `fetch`, or `pull`
uses the remote.

### `rm-remote [remote name]`

If no remote with the given name exists, print `A remote with that name does not
exist.` and stop. Otherwise delete `.gitlet/remotes/<remote name>`.

### `push [remote name] [remote branch name]`

Load the configured remote path. If the remote `.gitlet` directory does not
exist, print `Remote directory not found.` and stop. Then:

1. Let `localHead` be the current local branch head.
2. Look for the remote branch file in the remote repository's `refs/heads`.
3. If the remote branch exists, read `remoteHead`. If `remoteHead` is not an
   ancestor of `localHead` in the local commit graph, print `Please pull down
   remote changes before pushing.` and stop.
4. Copy every local commit and blob reachable from `localHead` into the remote
   repository if it is not already present there.
5. Create the remote branch if it does not exist.
6. Update the remote branch file so it points to `localHead`.

This command does not modify the local working directory, local `HEAD`, or the
local staging area.

### `fetch [remote name] [remote branch name]`

Load the configured remote path. If the remote `.gitlet` directory does not
exist, print `Remote directory not found.` and stop. If the remote repository
does not have the given branch, print `That remote does not have that branch.`
and stop. Otherwise:

1. Read the remote branch head commit id.
2. Copy every commit and blob reachable from that remote head into the local
   repository if it is not already present locally.
3. Create or update the local tracking branch named `[remote name]/[remote
   branch name]` so it points to the fetched remote head.

This command only changes local `.gitlet` metadata and object storage. It does
not change the working directory, current branch, or staging area.

### `pull [remote name] [remote branch name]`

Run the same logic as `fetch [remote name] [remote branch name]`. If fetch
fails, stop immediately after printing the fetch error. If fetch succeeds,
merge the local tracking branch `[remote name]/[remote branch name]` into the
current branch using the existing `merge` algorithm. Therefore `pull` has the
combined failure cases of `fetch` and `merge`, and it may modify the working
directory.

## Persistence

All persistent state is stored under `.gitlet` in the current working
directory. No in-memory state is trusted across command invocations.

```text
.gitlet/
  commits/
    <commit id>        serialized Commit
  blobs/
    <blob id>          raw file contents
  refs/
    heads/
      <branch name>    commit id at branch head
      <remote name>/
        <branch name>  fetched remote-tracking branch
  remotes/
    <remote name>      path to remote .gitlet directory
  HEAD                 current branch name
  index                serialized StagingArea
```

Commits and the staging area are saved with `Utils.writeObject` and loaded with
`Utils.readObject`. Blobs are saved as raw bytes with `Utils.writeContents`.
Branch files, remote files, and `HEAD` are small text files.

The id of a blob is `sha1(file bytes)`. The id of a commit is computed from the
commit metadata and the deterministic `TreeMap` of tracked blobs. Because
`TreeMap` and `TreeSet` have deterministic iteration order, status output and
commit id computation are stable.

Repository commands follow this persistence rule: load the required objects,
perform all error checks before mutating files, then write all changed state
before returning. Commands that fail due to user error should leave `.gitlet`
and the working directory unchanged, except for cases where the specification
explicitly requires deletion such as a successful `rm`.

Remote commands preserve the same object format in every repository. Copying
between local and remote repositories is just file copying between the two
repositories' `commits` and `blobs` directories. A remote path is considered
valid only when the configured `.gitlet` directory exists at command execution
time.
