package algos;

/**
 * Created by Denis on 25.02.14.
 */
class TreeNode implements Comparable<TreeNode> {
    public TreeNode left;
    public TreeNode right;
    public int quantity;
    public Byte character;
    public int size;

    public TreeNode(Byte character, int quantity) {
        this();
        size = 3;
        this.character = character;
        this.quantity = quantity;
    }

    public TreeNode() {
        left = right = null;
        character = null;
        size = 1;
        quantity = 0;
    }

    public TreeNode(TreeNode left, TreeNode right) {
        this();
        this.left = left;
        this.right = right;
        this.quantity = left.quantity + right.quantity;
        this.size = 1 + left.size + right.size;
    }

    @Override
    public int compareTo(TreeNode node) {
        return this.quantity - node.quantity;
    }
}
