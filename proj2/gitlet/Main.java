package gitlet;
import java.io.File;
import static gitlet.Utils.*;
import static gitlet.Repository.*;
import gitlet.Commit;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                if (isInitialized()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                }
                init();
                break;
            case "add":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String fileName = args[1];
                Repository.add(fileName);
                break;
            case "commit":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    if (args.length == 1 || args[1].equals("")) {
                        System.out.println("Please enter a commit message.");
                    } else {
                        System.out.println("Incorrect operands.");
                    }
                    System.exit(0);
                }
                String message = args[1];
                Repository.commit(message);
                break;
            case "rm":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }           
                fileName = args[1];
                Repository.remove(fileName);
                break;
            case "log":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);               
                }
                Repository.log();
                break;
            case "global-log":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);               
                }
                Repository.globalLog();        
                break;
            case "find":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                message = args[1];
                Repository.find(message);
                break;
            case "status":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.printStatus();           
                break;
            case "checkout":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length == 2) {
                    String branchName = args[1];
                    Repository.checkoutVersion3(branchName);
                } else if (args.length == 3 && args[1].equals("--")) {
                    fileName = args[2];
                    Repository.checkoutVersion1(fileName);
                } else if (args.length == 4 && args[2].equals("--")) {
                    String commitID = args[1];
                    fileName = args[3];
                    Repository.checkoutVersion2(commitID, fileName);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                   
                }
                break;
            case "branch":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                      
                }
                String branchName = args[1];
                Repository.branch(branchName);
                break;
            case "rm-branch":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                break;
            case "reset":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                break;
            case "merge":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static boolean isInitialized() {
        /** The current working directory. */
        File CWD = new File(System.getProperty("user.dir"));
        /** The .gitlet directory. */
        File GITLET_DIR = join(CWD, ".gitlet");

        /* check if the dir .gitlet exists */
        return GITLET_DIR.isDirectory();
    }
}
