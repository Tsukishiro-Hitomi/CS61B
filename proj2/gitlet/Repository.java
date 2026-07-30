package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import gitlet.Commit;

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
        /* 计算文件路径，检查是否存在 */
        File filePath = join(Repository.CWD, fileName);
        if (!filePath.exists() || !filePath.isFile()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        /* 读取文件内容，计算对应的 blobID */
        byte[] fileContent = readContents(filePath);
        String BlobID = sha1(fileContent);

        /* 读取暂存区，更新，保存 */
        StagingArea currentStage = Repository.readStage();
        currentStage.stageAddition(fileName, BlobID);
        Repository.saveStage(currentStage);
    }

    /* 读取当前的暂存区 */
    static StagingArea readStage() {
        return readObject(Repository.indexFile, StagingArea.class);
    }

    /* 保存当前的暂存区 */
    static void saveStage(StagingArea stage) {
        writeObject(Repository.indexFile, stage);
    }

    /* 读取当前的 HEAD 分支 */
    static String readHEAD() {
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
