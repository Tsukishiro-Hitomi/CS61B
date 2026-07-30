package gitlet;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

public class StagingArea implements Serializable {
    private TreeMap<String, String> additions;
    private TreeSet<String> removals;

    public StagingArea() {
        additions = new TreeMap<String, String> ();
        removals = new TreeSet<String> ();
    }

    /* 将文件加入暂存区 */
    public boolean stageAddition(String fileName, String BlobID) {
        /* 如果删除文件已经存在于删除区，则移除 */
        if (isStagedForRemovals(fileName)) {
            unstageRemoval(fileName);
        }
        /* 如果暂存区已经保存过该文件，则更新（先删除再插入） */
        if (isStagedForAddition(fileName)) {
            unstageAddition(fileName);
        }
        if (!isSameAsHEAD(fileName, BlobID)) {
            /* 增加区加入文件名 */
            additions.put(fileName, BlobID);
        }
    }

    public boolean isStagedForAddition(String fileName) {
        return additions.containsKey(fileName);
    }

    public boolean isStagedForRemovals(String fileName) {
        return removals.contains(fileName);
    }

    public void unstageAddition(String fileName) {
        additions.remove(fileName);
    }

    public void unstageRemoval(String fileName) {
        removals.remove(fileName);
    }

    /* 用于比较当前文件的 BlobID 是否与当前的 HEAD 提交相同 */
    private boolean isSameAsHEAD(String fileName, String BlobID) {
        String HeadBranch = Repository.readHEAD();
        String HeadCommitID = Repository.readBranchCommitID(HeadBranch);
        Commit HeadCommit = Repository.readCommit(HeadCommitID);
        String HeadBlobID = HeadCommit.findBlobID(fileName);
        /* 注意字符串的比较用.equals() */
        if (HeadBlobID != null && HeadBlobID.equals(BlobID)) {
            return true;
        }
        return false;
    }
    
}
