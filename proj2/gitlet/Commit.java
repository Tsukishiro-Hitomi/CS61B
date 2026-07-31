package gitlet;

// TODO: any imports you need here

import java.util.Date; // TODO: You'll likely use this in this class
import static gitlet.Utils.*;
import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** commit message. */
    private String message;
    /* commit date. */
    private Date date;
    /* 用 commit id 存储 parent 和 secondParent。 */
    private String parent;
    private String secondParent;
    /* blobs: 存储文件名到 blob id 的映射。 */
    private TreeMap<String, String> blobs;
    /* SHA-1 ID */
    private String ID;
    

    /* TODO: fill in the rest of this class. */
    Commit(String message) {
        this.message = message;
    }

    Commit(String message, String parent, String secondParent, TreeMap<String, String> blobs) {
        this.message = message;
        this.parent = parent;
        this.secondParent = secondParent;
        this.blobs = blobs;
        this.date = new Date();
        this.ID = computeID();
    }

    Commit() {
        
    }

    public void commit(String currentBranchName) {
        /* 在 commits 目录下新建同名 commit ，将自己序列化写入*/
        File commitFile = join(Repository.commitsDir, this.ID);
        writeObject(commitFile, this);

        /* 更新分支对应的 commit */
        Repository.updateBranch(currentBranchName, this.ID);
    }

    static void initialCommit() {
        Commit init = new Commit("initial commit");
        init.date = new Date(0);
        init.parent = null;
        init.secondParent = null;
        /* initial commit 的 blob 映射为空 */
        init.blobs = new TreeMap<>();

        init.ID = init.computeID();
        
        /* 在 commits 目录下新建与 id 同名的提交。
        该提交文件将 commit Object 序列化写入。 */
        File commitFile = join(Repository.commitsDir, init.ID);
        writeObject(commitFile, init);
        
        /* 在 heads 目录下新建名为 master 的分支，该分支指向当前的 id。*/
        File masterFile = join(Repository.headsDir, "master");
        writeContents(masterFile, init.ID);

        /* 当前 HEAD 指向 master */
        writeContents(Repository.HEADFile, "master");

        /* 在 index 中写入一个空的 StagingArea */
        writeObject(Repository.indexFile, new StagingArea());
    }

    public void printCommit() {

    }

    private String computeID() {
        String idToCompute = message + date + parent + secondParent + blobs;
        return sha1(idToCompute);
    }

    public String findBlobID(String fileName) {
        return blobs.get(fileName);
    }

    public TreeMap<String, String> visitBlobs() {
        return this.blobs;
    }
}
