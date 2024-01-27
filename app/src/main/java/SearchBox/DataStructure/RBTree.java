package SearchBox.DataStructure;

public class RBTree<T extends Comparable<T>> {
	public Node<T> root; // The root node of the tree

	public class Node<T extends Comparable<T>> {
		Colour colour;       // Node colour
		T value;             // Node key
		Node<T> parent;      // Parent node
		Node<T> l, r;        // Children nodes

		// Value node
		public Node(T value) {
			this.value = value;
			this.colour = Colour.RED;
			this.parent = null;

			// Initialise children leaf nodes
			this.l = new Node<>();
			this.r = new Node<>();
			this.l.parent = this;
			this.r.parent = this;
		}

		// Leaf node
		public Node() {
			this.value = null;
			this.colour = Colour.BLACK;
		}

		public T getValue() {
			return value;
		}

		// Insert node into tree, ignoring colour
		public void insert(Node<T> n) {

			if (this.value == null) {
				n.parent = this.parent;
				if (this.parent.l == this)
					this.parent.l = n;
				else
					this.parent.r = n;
			}

			else {
				if (this.value.compareTo(n.value) > 0) {
					this.l.insert(n);
				}
				else if (this.value.compareTo(n.value) < 0) {
					this.r.insert(n);
				}
				else
					return;
			}


		}

		// Rotate the node so it becomes the child of its right branch
        /*
            e.g.
                  [x]                    b
                 /   \                 /   \
               a       b     == >   [x]     f
              / \     / \           /  \
             c  d    e   f         a    e
                                  / \
                                 c   d
        */
		public void rotateLeft() {

			// Make parent (if it exists) and right branch point to each other
			if (parent != null) {
				// Determine whether this node is the left or right child of its parent
				if (parent.l.value == value) {
					parent.l = r;
				} else {
					parent.r = r;
				}
			}

			r.parent = parent;
			parent = r;
			// Take right node's left branch
			r = parent.l;
			r.parent = this;
			// Take the place of the right node's left branch
			parent.l = this;
			if (this.parent.parent == null) {
				root = (Node)this.parent;
			}
		}

		// Rotate the node so it becomes the child of its left branch
        /*
            e.g.
                  [x]                    a
                 /   \                 /   \
               a       b     == >     c     [x]
              / \     / \                   /  \
             c  d    e   f                 d    b
                                               / \
                                              e   f
        */
		public void rotateRight() {

			if (parent != null) {
				// Determine whether this node is the left or right child of its parent
				if (parent.l.value == value) {
					parent.l = l;
				} else {
					parent.r = l;
				}
			}
			l.parent = parent;

			parent = l;
			l = parent.r;
			l.parent = this;
			parent.r = this;
		}

	}

	// Initialise empty RBTree
	public RBTree() {
		root = null;
	}


	// Insert node into RBTree
	private void insert(Node<T> x) {

		// Insert node into tree
		if (root == null) {
			root = x;
		} else {
			root.insert(x);
		}

		// Fix tree
		while (x.value != root.value && x.parent.colour == Colour.RED) {
			boolean left = x.parent == x.parent.parent.l; // Is parent a left node
			Node<T> y = left ? x.parent.parent.r : x.parent.parent.l; // Get opposite "uncle" node to parent

			if (y.colour == Colour.RED) {
				// Case 1: Recolour
				x.parent.colour = Colour.BLACK;
				y.colour = Colour.BLACK;
				x.parent.parent.colour = Colour.RED;
				// Check if violated further up the tree
				x = x.parent.parent;
			} else {
				if (x.value == (left ? x.parent.r.value : x.parent.l.value)) {
					// Case 2 : Left (uncle is left node) / Right (uncle is right node) Rotation
					x = x.parent;
					if (left) {
						// Perform left rotation
						if (x.value == root.value) root = x.r; // Update root
						x.rotateLeft();
					} else {
						if (x.value == root.value) root = x.l; // Update root
						x.rotateRight();
						// This is part of the "then" clause where left and right are swapped
						// Perform right rotation
					}
				}
				// Adjust colours to ensure correctness after rotation
				x.parent.colour = Colour.BLACK;
				x.parent.parent.colour = Colour.RED;
				// Case 3 : Right (uncle is left node) / Left (uncle is right node) Rotation
				if (left) {
					if (x.parent.parent.value == root.value) root = x.parent;
					x.parent.parent.rotateRight();
					// Perform right rotation
				} else
				{
					if (x.parent.parent.value == root.value) root = x.parent;
					x.parent.parent.rotateLeft();
					// This is part of the "then" clause where left and right are swapped
					// Perform left rotation
				}
			}
		}

		// Ensure property 2 (root and leaves are black) holds
		root.colour = Colour.BLACK;
	}


	// (Safely) insert a key into the tree
	public void insert(T value) {
		Node<T> node = new Node<T>(value);
		if (node != null)
			insert(node);
	}

	public void remove(T value) {
		Node<T> node = search(value);
		if (node != null)
			deleteNode(node);
	}


	private void preOrder(Node<T> tree) {
		if(tree != null && tree.value != null) {
			System.out.print("(");
			preOrder(tree.l);
			System.out.print(tree.value.toString() +" ");
			preOrder(tree.r);
			System.out.print(")");
		}
	}

	public void preOrder() {
		preOrder(root);
		System.out.println();
	}

	// Return the corresponding node of a key, if it exists in the tree
	public Node<T> find(Node<T> x, T v) {
		if (x.value == null)
			return null;

		int cmp = v.compareTo(x.value);
		if (cmp < 0)
			return find(x.l, v);
		else if (cmp > 0)
			return find(x.r, v);
		else
			return x;
	}

	public Node<T> search(T key) {
		return find(root, key);
	}

	public enum Colour {
		RED,
		BLACK;
	}


	public void deleteNode(Node z){
		Node x;
		Node y = z;
		Colour y_original_color = y.colour;
		if (z.l.value == null){
			x = z.r;
			deleteTransplant(z,z.r);
		}else if (z.r.value == null){
			x = z.l;
			deleteTransplant(z,z.l);
		}else {
			y = treeMinimum(z.r);
			y_original_color = y.colour;
			x = y.r;
			if (y.parent == z) x.parent = y;
			else {
				deleteTransplant(y,y.r);
				y.r = z.r;
				y.r.parent = y;
			}
			deleteTransplant(z,y);
			y.l = z.l;
			y.l.parent = y;
			y.colour = z.colour;
		}
		if (y_original_color == Colour.BLACK){
			deleteFixedUP(x);
		}
	}

	/**
	 * Deletion
	 * @param x
	 */
	public  void deleteFixedUP(Node x){
		Node w;
		while (x.value != root.value && x.colour == Colour.BLACK){
			if (x == x.parent.l){
				w = x.parent.r;
				if (w.colour == Colour.RED){
					w.colour = Colour.BLACK;
					x.parent.colour = Colour.RED;
					x.parent.rotateLeft();
					w = x.parent.r;
				}
				if (w.l.colour == Colour.BLACK && w.r.colour == Colour.BLACK){
					w.colour = Colour.RED;
					x = x.parent;
					continue;
				}else if (w.r.colour == Colour.BLACK){
					w.l.colour = Colour.BLACK;
					w.colour = Colour.RED;
					w.rotateRight();
					w = x.parent.r;

				}
				w.colour = x.parent.colour;
				x.parent.colour = Colour.BLACK;
				w.r.colour = Colour.BLACK;
				x.parent.rotateLeft();
				x = root;
			}else {
				w = x.parent.l;
				if (w.colour == Colour.RED){
					w.colour = Colour.BLACK;
					x.parent.colour = Colour.RED;
					x.parent.rotateRight();
					w = x.parent.l;
				}

				if (w.r.colour == Colour.BLACK && w.l.colour == Colour.BLACK){
					w.colour = Colour.RED;
					x = x.parent;
					continue;
				}else if (w.l.colour == Colour.BLACK){
					w.r.colour = Colour.BLACK;
					w.colour = Colour.RED;
					w.rotateLeft();
					w = x.parent.l;
				}
				w.colour = x.parent.colour;
				x.parent.colour = Colour.BLACK;
				w.l.colour = Colour.BLACK;
				x.parent.rotateRight();
				x = root;
			}
		}
		x.colour = Colour.BLACK;
	}


	public void deleteTransplant(Node u,Node v){
		if(u.parent == null) root = v;
		else if(u == u.parent.l) u.parent.l = v;
		else u.parent.r = v;
		v.parent = u.parent;
	}


	public Node treeMinimum(Node x){
		while (x.l.value != null){
			x = x.l;
		}
		return x;
	}
}
