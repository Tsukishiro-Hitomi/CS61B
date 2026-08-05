package gitlet;

import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
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
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                String message = args[1];
                /* 对于正常 commit: 默认其 secondParent 为 null。只有在 merge 才会遇到 */
                Repository.commit(message, null);
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
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                      
                }
                branchName = args[1];
                Repository.removeBranch(branchName);
                break;
            case "reset":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                      
                }
                String commitID = args[1];
                Repository.reset(commitID);
                break;
            case "merge":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                      
                }
                branchName = args[1];
                Repository.merge(branchName);                
                break;
            case "add-remote":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }         
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                          
                }
                String remoteName = args[1];
                String toRemotePath = args[2];
                Repository.addRemote(remoteName, toRemotePath);
                break;
            case "rm-remote":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                          
                }
                remoteName = args[1];
                Repository.removeRemote(remoteName);
                break;
            case "push":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                          
                }
                remoteName = args[1];
                String remoteBranchName = args[2];
                Repository.push(remoteName, remoteBranchName);
                break;
            case "fetch":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                          
                }
                remoteName = args[1];
                remoteBranchName = args[2];
                Repository.fetch(remoteName, remoteBranchName);
                break;
            case "pull":
                if (!isInitialized()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);                          
                }
                remoteName = args[1];
                remoteBranchName = args[2];
                Repository.pull(remoteName, remoteBranchName);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static boolean isInitialized() {
        return Repository.GITLET_DIR.isDirectory();
    }
}
