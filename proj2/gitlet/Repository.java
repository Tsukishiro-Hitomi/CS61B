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
    public static final File IndexFile = join(GITLET_DIR, "index");

    /* TODO: fill in the rest of this class. */

    /* Established a .gitlet directory in the current working directory. */
    public static void init() {
        File commitsDir = join(GITLET_DIR, "commits");
        File blobsDir = join(GITLET_DIR, "blobs");
        File headsDir = join(GITLET_DIR, "refs", "heads");
        
        /* 注意：File API 为静默失败，mkdir 和 mkdirs 返回的是布尔值（文件夹创建是否成功）
        因此理论上来说应该用 if else 兜住失败。这里为简单实现，暂不考虑文件夹创建失败的情形。
        */
        GITLET_DIR.mkdir();
        commitsDir.mkdir();
        blobsDir.mkdir();

        /* 创建多级目录需要用 mkdirs 命令 */
        headsDir.mkdirs();

        /* 调用 initialCommit */
        Commit.initialCommit();
    }

    /* 保存当前的工作区 */
    public void saveStage(StagingArea stage) {

    }
    
}
