package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import gitlet.Commit;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.List;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

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
    static void commit(String message) {
        /* 检查当前暂存区是否为空 */
        StagingArea currentStage = StagingArea.readStage();
        if (currentStage.isEmptyStage()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        /* 读取当前 HEAD 分支对应的 commit 的内容 */
        String headBranch = readHEADBranch();
        String headCommitID = readBranchCommitID(headBranch);
        Commit headCommit = readCommit(headCommitID);

        /* 分析当前暂存区与 headCommit 对应的 blobs 差异 */
        TreeMap<String, String> blobs = headCommit.visitBlobs();
        TreeMap<String, String> additions = currentStage.visitAdditions();
        TreeSet<String> removals = currentStage.visitRemovals();

        for (String fileToRemove : removals) {  // 暂存区删除的文件：需要在 blobs 映射中删除
            blobs.remove(fileToRemove);
        }
        for (String fileToAdd : additions.keySet()) {  // 暂存区增加的文件：需要在 blobs 映射中添加/更新
            blobs.put(fileToAdd, additions.get(fileToAdd));
        }

        Commit newCommit = new Commit(message, headCommitID, null, blobs);
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
        System.out.println("=== Modifications Not Staged For commit ===");
        /* 情况一：被当前分支跟踪，在工作区发生变化，并且未被提交到暂存区 */


        /* 打印未被跟踪的内容 */
        System.out.println("=== Untracked Files ===");
    }

    /* status 的 helper function，用于查找情况一 */
    private static List<String> statusHelper1() {
        return null;
    }

    /* 检查文件是否存在 */
    private static boolean checkIsValidFile(String fileName) {
        File filePath = join(Repository.CWD, fileName);
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
        String commitID = readContentsAsString(commitIDFile);
        return commitID;
    }

    /* 读取某一次 commit 的内容 */
    static Commit readCommit(String commitID) {
        File commitFile = join(commitsDir, commitID);
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
}
