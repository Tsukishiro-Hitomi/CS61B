package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.Objects;
import gitlet.Commit;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;

// TODO: any imports you need here

/** Represents a gitlet repository.
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File commitsDir = join(GITLET_DIR, "commits");
    public static final File blobsDir = join(GITLET_DIR, "blobs");
    public static final File headsDir = join(GITLET_DIR, "refs", "heads");
    public static final File HEADFile = join(GITLET_DIR, "HEAD");
    public static final File indexFile = join(GITLET_DIR, "index");

    /* TODO: fill in the rest of this class. */

    /* 用于实现 init 方法 */
    static void init() {
        /* 注意：File API 为静默失败，mkdir 和 mkdirs 返回的是布尔值（文件夹创建是否成功）
        因此理论上来说应该用 if else 兜住失败。这里为简单实现，暂不考虑文件夹创建失败的情形。
        */
        Repository.GITLET_DIR.mkdir();
        Repository.commitsDir.mkdir();
        Repository.blobsDir.mkdir();

        /* 创建多级目录需要用 mkdirs 命令 */
        Repository.headsDir.mkdirs();

        /* 调用 initialCommit */
        Commit.initialCommit();
    }

    /* 用于实现 add 方法 */
    static void add(String fileName) {
        if (!checkIsValidFile(fileName)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        File filePath = join(Repository.CWD, fileName);

        /* 读取文件内容，计算对应的 blobID */
        byte[] fileContent = readContents(filePath);
        String BlobID = sha1(fileContent);

        /* 读取暂存区 */
        StagingArea currentStage = StagingArea.readStage();

        /* 如果该文件被成功加入暂存区，则在 BlobID 对应路径下保存该文件内容 */
        if (currentStage.stageAddition(fileName, BlobID)) {
            saveFileContent(fileName, BlobID);
        }

        /* 保存暂存区 */
        currentStage.saveStage();
    }

    /* 用于实现 commit 方法 */
    static void commit(String message, String secondParent) {
        /* 检查当前暂存区是否为空 */
        StagingArea currentStage = StagingArea.readStage();
        if (currentStage.isEmptyStage() && secondParent == null) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        /* 读取当前 HEAD 分支对应的 commit 的内容 */
        String headBranch = readHEADBranch();
        String headCommitID = readBranchCommitID(headBranch);
        Commit headCommit = readCommit(headCommitID);

        /* 分析当前暂存区与 headCommit 对应的 blobs 差异 */
        TreeMap<String, String> blobs = new TreeMap<>(headCommit.visitBlobs());
        TreeMap<String, String> additions = currentStage.visitAdditions();
        TreeSet<String> removals = currentStage.visitRemovals();

        for (String fileToRemove : removals) {  // 暂存区删除的文件：需要在 blobs 映射中删除
            blobs.remove(fileToRemove);
        }
        for (String fileToAdd : additions.keySet()) {  // 暂存区增加的文件：需要在 blobs 映射中添加/更新
            blobs.put(fileToAdd, additions.get(fileToAdd));
        }

        Commit newCommit = new Commit(message, headCommitID, secondParent, blobs);
        newCommit.commit(headBranch);

        /* 清空暂存区 */
        currentStage.clearStage();
    }

    /* 用于实现 rm 方法 */
    static void remove(String fileName) {
        boolean removed = false;
        /* 检查文件是否已经被提交到暂存区 */
        StagingArea currentStage = StagingArea.readStage();
        if (currentStage.isStagedForAddition(fileName)) {
            currentStage.unstageAddition(fileName);
            currentStage.saveStage();
            removed = true;
        }

        /* 检查文件是否被当前分支跟踪 */
        Commit headCommit = readHEADCommit();
        if (headCommit.existsFile(fileName)) {
            /* 将文件存入暂存区的删除区 */
            currentStage.stageRemoval(fileName);
            currentStage.saveStage();
            /* 如果文件存在于工作区目录中，则删除 */
            deleteFile(fileName);
            removed = true;
        }

        /* 如果以上两种情况未被覆盖，报错 */
        if (!removed) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /* 用于实现 log 方法 */
    static void log() {
        Commit currentCommit = readHEADCommit();
        while (currentCommit != null) {
            currentCommit.printCommit();
            String parentCommitID = currentCommit.visitParent();
            if (parentCommitID == null) {
                break;
            }
            currentCommit = readCommit(parentCommitID);
        }
    }

    /* 用于实现 global-log 方法 */
    static void globalLog() {
        List<String> commitsIDs = plainFilenamesIn(Repository.commitsDir);
        for (String commitID : commitsIDs) {
            Commit c = readCommit(commitID);
            c.printCommit();
        }
    }

    /* 用于实现 find 方法 */
    static void find(String message) {
        List<String> commitsIDs = plainFilenamesIn(Repository.commitsDir);
        boolean hasFound = false;
        for (String commitID : commitsIDs) {
            Commit c = readCommit(commitID);
            if (c.visitMessage().equals(message)) {
                System.out.println(c.visitID());
                hasFound = true;
            }
        }   
        if (!hasFound) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /* 用于实现 status 方法 */
    static void printStatus() {
        /* 打印所有分支 */
        System.out.println("=== Branches ===");
        List<String> branchNames = plainFilenamesIn(Repository.headsDir);
        String headBranch = readHEADBranch();
        for (String branchName : branchNames) {
            if (branchName.equals(headBranch)) {
                System.out.println(String.format("*%s", branchName));
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();

        /* 打印暂存区的文件 */
        System.out.println("=== Staged Files ===");
        StagingArea currentStage = StagingArea.readStage();
        for (String fileName : currentStage.visitAdditions().keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String fileName : currentStage.visitRemovals()) {
            System.out.println(fileName);
        }
        System.out.println();

        /* 打印被修改并且没有被加入暂存区的文件 */
        System.out.println("=== Modifications Not Staged For Commit ===");
        TreeSet<String> modified = new TreeSet<String> ();
        TreeSet<String> deleted = new TreeSet<String> ();

        /* 情况一：被当前分支跟踪，在工作区发生变化，并且未被提交到暂存区 */
        List<String> result = statusHelper1();
        for (String fileName : result) {
            modified.add(fileName);
        }
        /* 情况二：在暂存区的增加区中，但工作区内容与其不同 */
        result = statusHelper2();
        for (String fileName : result) {
            modified.add(fileName);
        }
        /* 情况三：在暂存区的增加区中，但在工作区中被删除 */
        result = statusHelper3();
        for (String fileName : result) {
            deleted.add(fileName);
        }
        /* 情况四：未在暂存区的删除区中，但被当前 HEAD 提交跟踪且在工作区中被删除 */
        result = statusHelper4();
        for (String fileName : result) {
            deleted.add(fileName);
        }

        for (String fileName : modified) {
            System.out.println(String.format("%s (modified)", fileName));
        }
        for (String fileName : deleted) {
            System.out.println(String.format("%s (deleted)", fileName));
        }
        System.out.println();


        /* 打印未被跟踪的内容 */
        System.out.println("=== Untracked Files ===");
        List<String> fileNames = plainFilenamesIn(Repository.CWD);
        for (String fileName : fileNames) {
            if (checkIsUntracked(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    /* 用于实现 checkout 的3种方法 */
    /* 将 HEAD 分支的该文件写入当前工作区 */
    static void checkoutVersion1(String fileName) {
        /* 调用 checkoutVersion2: 将 HEAD 分支对应的该文件写入 */

        String headBranch = readHEADBranch();
        String headCommitID = readBranchCommitID(headBranch);
        checkoutVersion2(headCommitID, fileName);
    }

    /* 将某个提交的该文件写入当前工作区 */
    static void checkoutVersion2(String commitID, String fileName) {
        /* 读取该提交 */
        commitID = abbreviatedID(commitID);
        if (commitID == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit c = readCommit(commitID);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!c.existsFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        checkoutFileFromCommit(c, fileName);
    }

    /* 将某个分支的全部文件写入当前工作区 */
    static void checkoutVersion3(String branchName) {
        /* 调用 checkoutVersion2: 将给分支对应提交的全部文件写入 */

        /* 检查是否与当前 HEAD 分支相同 */
        String currentHEADBranch = readHEADBranch();
        if (currentHEADBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        /* 检查该分支是否存在 */
        String branchCommitID = readBranchCommitID(branchName);
        if (branchCommitID == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        /* 遍历该提交的所有文件，写入工作区 */
        Commit c = readCommit(branchCommitID);
        checkoutAllFileFromCommit(c);

        // 更新 HEAD
        writeContents(Repository.HEADFile,branchName);

        // 清空暂存区
        StagingArea currentStage = StagingArea.readStage();
        currentStage.clearStage();
    }

    /* 将某个 commit 中的文件写入工作区，checkoutVersion1/2/3 的工具函数 */
    private static void checkoutFileFromCommit(Commit c, String fileName) {
        /* 读取该提交内存储的文件内容 */
        String blobID = c.findBlobID(fileName);
        byte[] blobContent = readBlobContent(blobID);

        File currentFile = join(Repository.CWD, fileName);
        writeContents(currentFile, blobContent);
    }

    /* 将某个 commit 的所有文件写入工作区，checkoutVersion3 的工具函数
    将 commitID 而非 branchName 作为接口，便于后面的 reset 方法复用 */
    private static void checkoutAllFileFromCommit(Commit c) {
        TreeMap<String, String> commitBlobs = c.visitBlobs();
        for (String fileName : commitBlobs.keySet()) {
            /* 检查文件是否未被跟踪 */
            if (checkIsUntracked(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        /* 删除工作区内当前分支跟踪而目标分支不跟踪的文件 */
        Commit currentCommit = readHEADCommit();
        for (String fileName : currentCommit.visitBlobs().keySet()) {
            if (!c.existsFile(fileName)) {
                deleteFile(fileName);
            }
        }

        /* 写入文件 */
        for (String fileName : commitBlobs.keySet()) {
            checkoutFileFromCommit(c, fileName);
        }
    }

    /* 用于实现 branch 方法 */
    static public void branch(String branchName) {
        File branchPath = join(Repository.headsDir, branchName);
        /* 检查是否有同名分支存在 */
        if (checkIsValidFile(branchPath)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        /* 新建分支，写入当前 HEAD 分支对应的 CommitID */
        String headBranch = readHEADBranch();
        String headBranchCommitID = readBranchCommitID(headBranch);
        writeContents(branchPath, headBranchCommitID);
    }

    /* 用于实现 rm-branch 方法 */
    static public void removeBranch(String branchName) {
        /* 检查是否为当前 HEAD 分支 */
        String currentHEADBranch = readHEADBranch();
        if (currentHEADBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        /* 检查该分支是否存在 */
        File branchPath = join(Repository.headsDir, branchName);
        if (!checkIsValidFile(branchPath)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        /* 删除该分支 */
        branchPath.delete();
    }

    /* 用于实现 reset 方法 */
    static public void reset(String commitID) {
        /* 检查该 commitID 是否存在 */
        commitID = abbreviatedID(commitID);
        if (commitID == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit c = readCommit(commitID);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        checkoutAllFileFromCommit(c);

        /* 更新 HEAD 分支指向该提交 */
        String headBranch = readHEADBranch();
        updateBranch(headBranch, commitID);

        // 清空暂存区
        StagingArea currentStage = StagingArea.readStage();
        currentStage.clearStage();
    }

    /* 用于实现 merge 方法 */
    static public void merge(String branchName) {
        StagingArea currentStage = StagingArea.readStage();
        if (!currentStage.isEmptyStage()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        String givenBranchCommitID = readBranchCommitID(branchName);
        if (givenBranchCommitID == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        };

        String currentBranch = readHEADBranch();
        String currentBranchCommitID = readBranchCommitID(currentBranch);
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit currentCommit = readHEADCommit();
        Commit givenCommit = readCommit(givenBranchCommitID);

        /* 寻找分裂点 */
        String splitPointID = findSplitPoint(currentBranchCommitID, givenBranchCommitID);
        Commit splitPoint = readCommit(splitPointID);
        
        /* 分裂点是要合并的分支 Commit：说明该 Commit 是当前分支的祖先，无需合并 */
        if (splitPointID.equals(givenBranchCommitID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        /* 分裂点是当前 HEAD 分支 Commit：快进当前分支到目标分支 */
        if (splitPointID.equals(currentBranchCommitID)) {
            checkoutAllFileFromCommit(givenCommit);
            updateBranch(currentBranch, givenBranchCommitID);
            currentStage.clearStage();
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        /* 保守检查未追踪文件 */
        for (String fileName : givenCommit.visitBlobs().keySet()) {
            if (checkIsUntracked(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        TreeSet<String> allFileNames = findAllFileNames(splitPoint, currentCommit, givenCommit);
        boolean hasMetConflict = false;
        /* 对文件逐个处理，并记录是否遇到冲突 */
        for (String fileName : allFileNames) {
            if (handleFile(fileName, splitPoint, currentCommit, givenCommit)) {
                hasMetConflict = true;
            }
        }

        /* 调用 commit 方法 */
        String commitMessage = String.format("Merged %s into %s.", branchName, currentBranch);
        commit(commitMessage, givenBranchCommitID);
        
        if (hasMetConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /* 对 commitID 实现前缀匹配 */
    static private String abbreviatedID(String commitID) {
        if (commitID.length() == 40) {
            return commitID;
        }
        List<String> commitIDs = plainFilenamesIn(Repository.commitsDir);
        for (String fullcommitID : commitIDs) {
            if (fullcommitID.startsWith(commitID)) {
                return fullcommitID;
            }
        }
        return null;
    }

    /* status 方法情况一：被当前分支跟踪，在工作区发生变化，且未被提交到暂存区 */
    static private List<String> statusHelper1() {
        List<String> result = new ArrayList<String> ();
        Commit c = readHEADCommit();
        TreeMap<String, String> b = c.visitBlobs();

        StagingArea currentStage = StagingArea.readStage();
        for (Map.Entry<String, String> e : b.entrySet()) {
            String fileName = e.getKey();
            String fileContentSHA1 = e.getValue();

            /* 排除掉已经被提交到暂存区和不存在于工作区的文件 */
            if (currentStage.isStagedForAddition(fileName)) {
                continue;
            }
            if (!checkIsValidFile(fileName)) {
                continue;
            }

            /* 通过 sha1 对比工作区的文件内容与 commit 中记录的文件内容 */
            File filePath = join(Repository.CWD, fileName);
            byte[] currentContent = readContents(filePath);
            String currentContentSHA1 = sha1(currentContent);

            if (!fileContentSHA1.equals(currentContentSHA1)) {
                result.add(fileName);
            }
        }

        return result;
    }

    /* status 方法情况二： 在暂存区的增加区中，但工作区内容与其不同 */
    static private List<String> statusHelper2() {
        List<String> result = new ArrayList<String> ();
        StagingArea currentStage = StagingArea.readStage();
        TreeMap<String, String> additions = currentStage.visitAdditions();
        for (Map.Entry<String, String> e : additions.entrySet()) {
            String fileName = e.getKey();
            String fileContentSHA1 = e.getValue();    
            if (!checkIsValidFile(fileName)) {
                continue;
            }
            /* 通过 sha1 对比工作区的文件内容与 commit 中记录的文件内容 */
            File filePath = join(Repository.CWD, fileName);
            byte[] currentContent = readContents(filePath);
            String currentContentSHA1 = sha1(currentContent);

            if (!fileContentSHA1.equals(currentContentSHA1)) {
                result.add(fileName);
            }
        }
        return result;
    }

    /* status 方法情况三： 在暂存区的增加区中，但在工作区中已被删除 */
    static private List<String> statusHelper3() {
        List<String> result = new ArrayList<String> ();
        StagingArea currentStage = StagingArea.readStage();
        TreeMap<String, String> additions = currentStage.visitAdditions();
        for (Map.Entry<String, String> e : additions.entrySet()) {
            String fileName = e.getKey();
            String fileContentSHA1 = e.getValue();    
            if (!checkIsValidFile(fileName)) {
                result.add(fileName);
            }
        }
        return result;
    }

    /* status 方法情况四：未在暂存区的删除区中，但被当前 HEAD 提交跟踪且在工作区中被删除 */
    static private List<String> statusHelper4() {
        List<String> result = new ArrayList<String> ();
        Commit c = readHEADCommit();
        TreeMap<String, String> b = c.visitBlobs();

        StagingArea currentStage = StagingArea.readStage();
        for (Map.Entry<String, String> e : b.entrySet()) {
            String fileName = e.getKey();
            String fileContentSHA1 = e.getValue();
            
            /* 排除已提交到删除区的文件 */
            if (currentStage.isStagedForRemovals(fileName)) {
                continue;
            }

            if (!checkIsValidFile(fileName)) {
                result.add(fileName);
            }
        } 
        return result;
    }

    /* merge 方法：用于寻找分裂点 */
    static private String findSplitPoint(String currentCommitID, String objectCommitID) {
        TreeMap<String, Integer> currentCommitAncestors = findAncestors(currentCommitID);
        TreeMap<String, Integer> objectCommitAncestors = findAncestors(objectCommitID);
        int minDistance = Integer.MAX_VALUE;
        String resultCommitID = null;

        /* 寻找距离最短的点作为最近的公共祖先节点 */
        for (Map.Entry<String, Integer> e : currentCommitAncestors.entrySet()) {
            String commitID = e.getKey();
            int distance = e.getValue();

            if (objectCommitAncestors.containsKey(commitID)) {
                int totalDistance = distance + objectCommitAncestors.get(commitID);
                if (totalDistance < minDistance) {
                    resultCommitID = commitID;
                    minDistance = totalDistance;
                }
            }
        }
        return resultCommitID;
    }

    /* 使用 BFS 记录所有祖先节点以及与当前节点的距离 */
    static private TreeMap<String, Integer> findAncestors(String commitID) {
        TreeMap<String, Integer> result = new TreeMap<String, Integer> ();
        int distance = 0;
        Deque<String> record = new ArrayDeque<String>();
        /* 对于 DAG 的 BFS 遍历，需要记录已经遍历了哪些节点，避免重复遍历导致无限循环 */
        HashSet<String> seen = new HashSet<String> ();

        record.addLast(commitID);
        while (!record.isEmpty()) {
            int currentSize = record.size();
            for (int i = 0; i < currentSize; i++) {
                String currentCommitID = record.removeFirst();

                /* 避免重复遍历 */
                if (seen.contains(currentCommitID)) {
                    continue;
                }

                result.put(currentCommitID, distance);
                seen.add(currentCommitID);

                Commit currentCommit = readCommit(currentCommitID);
                String parent = currentCommit.visitParent();
                String secondParent = currentCommit.visitSecondParent();

                if (parent != null) {
                    record.addLast(parent);
                }

                if (secondParent != null) {
                    record.addLast(secondParent);
                }
            }
            distance += 1;
        }
        return result;
    }

    /* 用于获取三个提交的文件并集 */
    static private TreeSet<String> findAllFileNames(Commit splitPoint, Commit currentCommit, Commit givenCommit) {
        TreeSet<String> result = new TreeSet<String> ();
        for (String fileName : splitPoint.visitBlobs().keySet()) {
            result.add(fileName);
        }
        for (String fileName : currentCommit.visitBlobs().keySet()) {
            result.add(fileName);
        }
        for (String fileName : givenCommit.visitBlobs().keySet()) {
            result.add(fileName);
        }
        return result;
    }

    /* merge 核心：对文件逐个处理 */
    static private boolean handleFile(String fileName, Commit splitPoint, Commit currentCommit, Commit givenCommit) {
        String splitPointBlobID = splitPoint.findBlobID(fileName);
        String currentCommitBlobID = currentCommit.findBlobID(fileName);
        String givenCommitBlobID = givenCommit.findBlobID(fileName);

        StagingArea currentStage = StagingArea.readStage();

        /* 都被删除 */
        if (currentCommitBlobID == null && givenCommitBlobID == null) {
            return false;
        }

        /* 相同修改 */  
        if (Objects.equals(currentCommitBlobID, givenCommitBlobID)) {
            return false;
        }

        /* 只有 current 分支改动 */
        if (!Objects.equals(currentCommitBlobID, splitPointBlobID) && Objects.equals(givenCommitBlobID, splitPointBlobID)) {
            return false;
        }

        /* 只有 given 分支改动 */
        if (Objects.equals(currentCommitBlobID, splitPointBlobID) && !Objects.equals(givenCommitBlobID, splitPointBlobID)) {
            /* 如果 given 分支将该文件删除 */
            if (givenCommitBlobID == null) {
                /* 从工作区删除，添加到暂存区的删除区 */
                currentStage.stageRemoval(fileName);
                deleteFile(fileName);
            } else {
                /* 如果只是修改：checkout given 分支的版本，添加到暂存区的添加区 */
                String blobID = givenCommit.findBlobID(fileName);
                currentStage.stageAddition(fileName, blobID);
                checkoutFileFromCommit(givenCommit, fileName);
            }
            currentStage.saveStage();
            return false;
        }

        /* 遇到冲突 */
        String currentContents = readBlobContentAsString(currentCommitBlobID);
        String givenContents = readBlobContentAsString(givenCommitBlobID);
        String conflict = "<<<<<<< HEAD\n" + currentContents + "=======\n" + givenContents + ">>>>>>>\n";

        /* 将冲突内容写入工作区文件，并提交到暂存区的增加区 */
        File filePath = join(Repository.CWD, fileName);
        writeContents(filePath, conflict);
        String blobID = computeBlobID(fileName);
        /* 保存新文件内容到对应 blob 路径 */
        saveFileContent(fileName, blobID);
        currentStage.stageAddition(fileName, blobID);
        currentStage.saveStage();
        return true;
    }

    /* 检查文件是否存在 */
    static private boolean checkIsValidFile(String fileName) {
        File filePath = join(Repository.CWD, fileName);
        return filePath.exists() && filePath.isFile();
    }

    static private boolean checkIsValidFile(File filePath) {
        return filePath.exists() && filePath.isFile();   
    }

    /* 从工作区中删除文件 */
    private static void deleteFile(String fileName) {
        if (checkIsValidFile(fileName)) {
            File filePath = join(Repository.CWD, fileName);
            restrictedDelete(filePath);
        }
    }

    /* 读取当前的 HEAD 分支 */
    static String readHEADBranch() {
        String headBranch = readContentsAsString(Repository.HEADFile);
        return headBranch;
    }

    /* 读取某个分支对应的 commitID */
    static String readBranchCommitID(String branch) {
        File commitIDFile = join(headsDir, branch);
        if (!checkIsValidFile(commitIDFile)) {
            return null;
        }
        String commitID = readContentsAsString(commitIDFile);
        return commitID;
    }

    /* 读取某一次 commit 的内容 */
    static Commit readCommit(String commitID) {
        File commitFile = join(commitsDir, commitID);
        if (!checkIsValidFile(commitFile)) {
            return null;
        }
        Commit c = readObject(commitFile, Commit.class);
        return c;
    }

    /* 读取当前 HEAD 分支对应的 commit */
    static Commit readHEADCommit() {
        String headBranch = readHEADBranch();
        String headBranchCommitID = readBranchCommitID(headBranch);
        return readCommit(headBranchCommitID);
    }

    /* 更新某个分支对应的 commit */
    static void updateBranch(String branchName, String commitID) {
        File branchPath = join(Repository.headsDir, branchName);
        writeContents(branchPath, commitID);
    }

    /* 将文件内容保存至 blobs */
    static void saveFileContent(String fileName, String BlobID) {
        File filePath = join(Repository.CWD, fileName);
        if (!filePath.exists() || !filePath.isFile()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        
        byte[] fileContent = readContents(filePath);        
        File blobPath = join(Repository.blobsDir, BlobID);
        writeContents(blobPath, fileContent);
    }

    /* 根据 ID 读取某个 blob 内容 */
    static byte[] readBlobContent(String blobID) {
        File blobPath = join(Repository.blobsDir, blobID);
        if (!checkIsValidFile(blobPath)) {
            return null;
        }
        byte[] blobContent = readContents(blobPath);
        return blobContent;
    }

    static String readBlobContentAsString(String blobID) {
        if (blobID == null) {
            return "";
        }
        File blobPath = join(Repository.blobsDir, blobID);
        String blobContent = readContentsAsString(blobPath);
        return blobContent;       
    }

    /* 根据文件名计算对应的 blobID */
    static String computeBlobID(String fileName) {
        File filePath = join(Repository.CWD, fileName);
        byte[] fileContents = readContents(filePath);
        String blobID = sha1(fileContents);
        return blobID;
    }

    /* 检查工作区文件是否未被跟踪
    未跟踪含义：存在于工作区，不在 HEAD 对应的 Commit 中，不在暂存区中 */
    static boolean checkIsUntracked(String fileName) {
        File filePath = join(Repository.CWD, fileName);
        if (!checkIsValidFile(fileName)) {
            return false;
        }
        Commit headCommit = readHEADCommit();
        if (headCommit.existsFile(fileName)) {
            return false;
        }
        StagingArea currentStage = StagingArea.readStage();
        if (currentStage.isStagedForAddition(fileName)) {
            return false;
        }
        return true;
    }
}
