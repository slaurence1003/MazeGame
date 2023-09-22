import java.util.*;
import java.util.Map.Entry;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// class representing a node
class Node {
    Posn posn;
    Color color;
    Node top;
    Node bottom;
    Node right;
    Node left;

    Node(Posn posn, Node top, Node bottom, Node right, Node left, Color color) {
        this.posn = posn;
        this.top = top;
        this.bottom = bottom;
        this.right = right;
        this.left = left;
        this.color = color;
    }

    Node() {
    }

    // Constructor for testing
    Node(Posn posn, Color color) {
        this.posn = posn;
        this.color = color;
    }

    // checks if the two nodes are equal to each other
    @Override
    public boolean equals(Object n) {
        if (n instanceof Node) {
            Node node = (Node) n;
            return this.posn.x == node.posn.x && this.posn.y == node.posn.y;
        }
        else {
            return false;
        }
    }

    // overrides the hashcode
    @Override
    public int hashCode() {
        return this.posn.x * this.posn.y;
    }

    // draws the current node
    public void drawNode(WorldScene background, int xSize, int ySize) {
        WorldImage cell;
        // bottom right cell
        if (this.posn.x == xSize - 1 && this.posn.y == ySize - 1) {
            cell = new RectangleImage(20, 20, "solid", Color.magenta);
        }
        else {
            cell = new RectangleImage(20, 20, "solid", this.color);
        }
        background.placeImageXY(cell, this.posn.x * 20 + 10, this.posn.y * 20 + 10);
    }
}

// edge class
class Edge implements Comparable<Edge> {
    Node from;
    Node to;
    int weight;

    Edge(Node from, Node to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    // draws the given edge
    public void drawEdge(WorldScene background) {
        WorldImage wall;
        // horizontal wall
        if (this.to.posn.x == this.from.posn.x) {
            wall = new RectangleImage(20, 2, "solid", Color.black);
        }
        // vertical wall
        else {
            wall = new RectangleImage(2, 20, "solid", Color.black);
        }
        int xVal = (this.from.posn.x * 20 + this.to.posn.x * 20) / 2;
        int yVal = (this.from.posn.y * 20 + this.to.posn.y * 20) / 2;

        background.placeImageXY(wall, xVal + 10, yVal + 10);
    }

    // compares the two weights
    public int compareTo(Edge o) {
        return Integer.compare(this.weight, o.weight);
    }

    // overrides the equals method to compare the two edges regardless of weight
    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge edge = (Edge) o;
            return this.to.equals(edge.to) && this.from.equals(edge.from);
        }
        else {
            return false;
        }
    }

    // overrides the hashcode
    @Override
    public int hashCode() {
        return this.to.posn.x * this.from.posn.y;
    }
}

// interface that encompasses queue and stack
interface ICollection<T> {

    // checks if the worklist is empty
    boolean isEmpty();

    // removes the next node and returns it
    Node remove();

    // adds the given node in the correct location depending if its queue or stack
    void add(Node node);

    // checks the size of the collection
    int size();
}

// queue class
class Queue implements ICollection<Node> {
    ArrayList<Node> worklist;

    Queue() {
        this.worklist = new ArrayList<Node>();
    }

    // checks if the worklist is empty
    public boolean isEmpty() {
        return this.worklist.isEmpty();
    }

    // removes the next node and returns it
    public Node remove() {
        return this.worklist.remove(0);
    }

    // adds the node to the back of the list
    public void add(Node n) {
        this.worklist.add(n);
    }

    // checks the size of the collection
    public int size() {
        return this.worklist.size();
    }
}

// stack class
class Stack implements ICollection<Node> {
    ArrayList<Node> worklist;

    Stack() {
        this.worklist = new ArrayList<Node>();
    }

    // checks if the worklist is empty
    public boolean isEmpty() {
        return this.worklist.isEmpty();
    }

    // removes the next node and returns it
    public Node remove() {
        return this.worklist.remove(0);
    }

    // adds the node to the front of the list
    public void add(Node n) {
        this.worklist.add(0, n);
    }

    //checks the size of the collection
    public int size() {
        return this.worklist.size();
    }
}

// class to represent the maze world
class MazeWorld extends World {
    ArrayList<ArrayList<Node>> nodes;
    ArrayList<Edge> worklist;
    ArrayList<Edge> edgesInTree;
    HashMap<Node, Node> representatives;
    int xVal;
    int yVal;
    Random rand;
    boolean breadthFirst;
    HashMap<Node, Node> cameFromEdge;
    ArrayList<Node> alreadyVisited;
    ArrayList<Node> reconstructList;
    WorldScene background;
    ICollection<Node> w;
    int num;
    Node current;

    MazeWorld(int xVal, int yVal, boolean breadthFirst) {
        this.xVal = xVal;
        this.yVal = yVal;
        this.nodes = initMaze();
        this.rand = new Random();
        this.representatives = new HashMap<Node, Node>();
        this.worklist = new ArrayList<Edge>();
        this.breadthFirst = breadthFirst;
        this.alreadyVisited = new ArrayList<Node>();
        this.cameFromEdge = new HashMap<Node, Node>();
        this.reconstructList = new ArrayList<Node>();
        makeEdgesInTree();
        this.background = new WorldScene(1200, 800);
        this.num = 0;
    }

    MazeWorld(int xVal, int yVal, Random r, boolean breadthFirst) {
        this.xVal = xVal;
        this.yVal = yVal;
        this.nodes = initMaze();
        this.rand = r;
        this.representatives = new HashMap<Node, Node>();
        this.worklist = new ArrayList<Edge>();
        this.breadthFirst = breadthFirst;
        this.alreadyVisited = new ArrayList<Node>();
        this.cameFromEdge = new HashMap<Node, Node>();
        this.reconstructList = new ArrayList<Node>();
        makeEdgesInTree();
        this.background = new WorldScene(1200, 800);
        this.num = 0;
    }

    // constructor for tests
    MazeWorld() {
    }

    // initialize nodes list with empty node
    public ArrayList<ArrayList<Node>> initMaze() {
        ArrayList<ArrayList<Node>> result = new ArrayList<ArrayList<Node>>();
        for (int j = 0; j <= this.yVal; j++) {
            ArrayList<Node> row = new ArrayList<Node>();
            for (int i = 0; i <= this.xVal; i++) {
                // top left
                if (i == 0 && j == 0) {
                    row.add(new Node(new Posn(i, j), null, null, null, null, Color.green));
                }
                // top row
                if (i > 0 && j == 0) {
                    row.add(new Node(new Posn(i, j), null, null, null, row.get(i - 1), Color.white));
                }
                // first column
                if (i == 0 && j > 0) {
                    row.add(
                            new Node(new Posn(i, j), result.get(j - 1).get(i), null, null, null, Color.white));
                }
                // everything else
                if (i > 0 && j > 0) {
                    row.add(new Node(new Posn(i, j), result.get(j - 1).get(i), null, null, row.get(i - 1),
                            Color.white));
                }
            }
            result.add(row);
        }

        // sets each nodes bottom and right
        for (int j = 0; j <= this.yVal; j++) {
            for (int i = 0; i <= this.xVal; i++) {
                Node currNode = result.get(j).get(i);
                if (currNode.posn.x < xVal && i < xVal) {
                    currNode.right = result.get(j).get(i + 1);
                }
                if (currNode.posn.y < yVal && j < yVal) {
                    currNode.bottom = result.get(j + 1).get(i);
                }
            }
        }
        return result;
    }

    // initializes the list of edges
    public ArrayList<Edge> initEdges() {
        ArrayList<Edge> result = new ArrayList<Edge>();
        for (int j = 0; j <= this.yVal; j++) {
            for (int i = 0; i <= this.xVal; i++) {
                // last column
                if (i == this.xVal + 1 && j < this.yVal) {
                    // adds edge going to the bottom
                    result.add(
                            new Edge(this.nodes.get(j).get(i), this.nodes.get(j + 1).get(i), this.getWeight()));
                }
                // last row
                if (i < this.xVal && j == this.yVal + 1) {
                    // adds edge going to the right
                    result.add(
                            new Edge(this.nodes.get(j).get(i), this.nodes.get(j).get(i + 1), this.getWeight()));
                }
                // everything
                if (i < this.xVal && j < this.yVal) {
                    // adds edge going to the bottom
                    result.add(
                            new Edge(this.nodes.get(j).get(i), this.nodes.get(j + 1).get(i), this.getWeight()));
                    // adds edge going to the right
                    result.add(
                            new Edge(this.nodes.get(j).get(i), this.nodes.get(j).get(i + 1), this.getWeight()));
                }
            }
        }
        return result;
    }

    // gets a unique random weight
    public int getWeight() {
        return rand.nextInt(this.xVal * this.yVal * 10);
    }

    // sorts the list of edges
    public ArrayList<Edge> sortEdges(ArrayList<Edge> edges) {
        Collections.sort(edges);
        return edges;
    }

    // initializes the representatives to themselves
    public void initReps() {
        for (int i = 0; i < this.nodes.size(); i++) {
            for (int j = 0; j < this.nodes.get(i).size(); j++) {
                Node currNode = this.nodes.get(i).get(j);
                this.representatives.put(currNode, currNode);
            }
        }
    }

    // finds the representative of the given node
    public Node find(Node n) {
        if (this.representatives.get(n).equals(n)) {
            return n;
        }
        else {
            return this.find(this.representatives.get(n));
        }
    }

    // unions the two nodes together
    public void union(Node n1, Node n2) {
        this.representatives.put(n1, n2);
    }

    // checks if there are multiple trees in the hashmap
    public boolean hasMultipleTrees() {
        int trees = 0;
        for (Entry<Node, Node> currNode : this.representatives.entrySet()) {
            if (currNode.getKey().equals(currNode.getValue())) {
                trees++;
            }
        }
        return trees > 1;
    }

    // creates the list of edges for the maze
    public void makeEdgesInTree() {
        this.edgesInTree = new ArrayList<Edge>();
        this.worklist = this.initEdges();
        this.worklist = this.sortEdges(this.worklist);
        this.initReps();
        int i = 0;
        while (this.hasMultipleTrees() && i < this.worklist.size()) {
            Edge currEdge = this.worklist.get(i);
            if (this.find(currEdge.to).equals(this.find(currEdge.from))) {
                i++;
            }
            else {
                this.edgesInTree.add(currEdge);
                this.union(this.find(currEdge.to), this.find(currEdge.from));
                i++;
            }
        }
    }

    // searches the tree either depth or breadth first
    public void searchTree() {
        if (breadthFirst) {
            w = new Stack();
        }
        else {
            w = new Queue();
        }
        w.add(this.nodes.get(0).get(0));
        while (!w.isEmpty()) {
            Node next = w.remove();
            // if the next node is the target reconstruct the path
            if (next.equals(this.nodes.get(this.yVal - 1).get(this.xVal - 1))) {
                this.reconstruct(next);
                break;
            }
            // otherwise add the nodes neighbors to the worklist(if they haven't already
            // been processed) and add the node it came from to the
            // cameFrom list
            else {
                if (next.top != null && this.edgesInTree.contains(new Edge(next.top, next, 1))
                        && !alreadyVisited.contains(next.top)) {
                    w.add(next.top);
                    this.cameFromEdge.put(next.top, next);
                }
                if (next.bottom != null && this.edgesInTree.contains(new Edge(next, next.bottom, 1))
                        && !alreadyVisited.contains(next.bottom)) {
                    w.add(next.bottom);
                    this.cameFromEdge.put(next.bottom, next);
                }
                if (next.left != null && this.edgesInTree.contains(new Edge(next.left, next, 1))
                        && !alreadyVisited.contains(next.left)) {
                    w.add(next.left);
                    this.cameFromEdge.put(next.left, next);
                }
                if (next.right != null && this.edgesInTree.contains(new Edge(next, next.right, 1))
                        && !alreadyVisited.contains(next.right)) {
                    w.add(next.right);
                    this.cameFromEdge.put(next.right, next);
                }
                this.alreadyVisited.add(next);
            }
        }
    }

    // reconstructs the path from end to beginning
    public void reconstruct(Node n) {
        while (!n.equals(this.nodes.get(0).get(0))) {
            n = this.cameFromEdge.get(n);
            this.reconstructList.add(n);
        }
    }

    // draws the scene
    public WorldScene makeScene() {
        this.searchTree();
        // draws nodes
        for (int i = 0; i < this.nodes.size() - 1; i++) {
            for (int k = 0; k < this.nodes.get(i).size() - 1; k++) {
                Node n = this.nodes.get(i).get(k);
                n.drawNode(background, this.xVal, this.yVal);
            }
        }
        // draws edges
        for (int j = 0; j < this.worklist.size(); j++) {
            Edge e = this.worklist.get(j);
            if (!this.edgesInTree.contains(e)) {
                e.drawEdge(background);
            }
        }
        // frame
        background.placeImageXY(new RectangleImage(2, this.yVal * 20, "Solid", Color.black),
                this.xVal * 20, this.yVal * 20 / 2);
        background.placeImageXY(new RectangleImage(this.xVal * 20, 2, "solid", Color.black),
                this.xVal * 20 / 2, this.yVal * 20);

        return background;
    }

    // handles the on tick method
    public void onTick() {
        Node n = this.alreadyVisited.get(num);
        n.color = Color.blue;
        if (this.reconstructList.contains(n)) {
            n.color = Color.red;
        }
        num++;
    }

    //resets the board
    public void onKeyEvent(String s) {
        if (s.equals("r")) {
            MazeWorld reset = new MazeWorld(xVal, xVal, false);
            this.nodes = reset.nodes;
            this.edgesInTree = reset.edgesInTree;
            this.current = reset.current;
            this.alreadyVisited = reset.alreadyVisited;
            this.breadthFirst = reset.breadthFirst;
            this.reconstructList = reset.reconstructList;
            reset.makeScene();
            if (s.equals("b")) {
                new MazeWorld(this.xVal, this.yVal, true);
            }
            if (s.equals("d")) {
                new MazeWorld(this.xVal, this.yVal, true);
            }
        }
    }

}

//examples class
class ExamplesMaze {
    MazeWorld w1;
    MazeWorld w2;
    MazeWorld empty;
    ArrayList<Edge> exEdges;
    MazeWorld m;
    Node a;
    Node b;
    Edge e;
    Edge c;
    int five;
    int two;
    Posn grass;
    WorldScene background = new WorldScene(50, 50);
    WorldScene makeScene;
    Stack w;
    Queue q;

    void initMaze() {
        this.m = new MazeWorld(five, five, false);
        MazeWorld empty = new MazeWorld();
        w1 = new MazeWorld(2, 2, false);
        w2 = new MazeWorld(3, 3, new Random(4), false);
        exEdges = new ArrayList<Edge>();
        Node a = new Node(new Posn(50, 50), Color.red);
        Node b = new Node(new Posn(50, 50), Color.blue);
        Edge e = new Edge(a, b, 5);
        Edge c = new Edge(b, a, 4);
        Posn grass = new Posn(10, 10);
    }

    // tests initMaze method
    void testInitMaze(Tester t) {
        initMaze();
        t.checkExpect(w1.nodes.get(0).get(0).top, null);
        t.checkExpect(w1.nodes.get(0).get(0).left, null);
    }

    // tests initEdges method
    void testInitEdges(Tester t) {
        initMaze();
        ArrayList<Edge> edges = w1.worklist;
        ArrayList<Edge> edges2 = w2.worklist;
        t.checkExpect(edges.size(), 8);
        t.checkExpect(edges2.size(), 18);
    }

    // tests the get weight method
    void testGetWeight(Tester t) {
        initMaze();
        t.checkExpect(w2.getWeight(), 82);
        t.checkExpect(w2.getWeight(), 48);
    }

    // tests the sort edges method
    void testSortEdges(Tester t) {
        initMaze();
        exEdges.add(new Edge(new Node(), new Node(), 1));
        t.checkExpect(w1.sortEdges(exEdges), exEdges);
        exEdges.add(new Edge(new Node(), new Node(), 3));
        exEdges.add(new Edge(new Node(), new Node(), 2));
        ArrayList<Edge> sortedEdges = new ArrayList<Edge>();
        sortedEdges.add(new Edge(new Node(), new Node(), 1));
        sortedEdges.add(new Edge(new Node(), new Node(), 2));
        sortedEdges.add(new Edge(new Node(), new Node(), 3));
        t.checkExpect(w1.sortEdges(exEdges), sortedEdges);
        exEdges.add(new Edge(new Node(), new Node(), 1));
        sortedEdges.add(0, new Edge(new Node(), new Node(), 1));
        t.checkExpect(w1.sortEdges(exEdges), exEdges);
    }

    // tests the find and union methods
    void testFindAndUnion(Tester t) {
        initMaze();
        Node n1 = w1.nodes.get(0).get(0);
        Node n2 = w1.nodes.get(0).get(1);
        Node n3 = w1.nodes.get(1).get(0);
        Node n4 = w1.nodes.get(1).get(1);

        t.checkExpect(w1.find(n4), n1);

        w1.union(w1.find(n1), w1.find(n2));
        t.checkExpect(w1.find(n1), w1.find(n2));
        t.checkExpect(w1.find(n2), w1.find(n1));
        t.checkExpect(w1.find(n3), n1);
        t.checkExpect(w1.find(n4), n1);

        w1.union(w1.find(n2), w1.find(n3));
        t.checkExpect(w1.find(n1), w1.find(n2));
        t.checkExpect(w1.find(n2), w1.find(n3));
        t.checkExpect(w1.find(n3), w1.find(n1));

        w1.union(w1.find(n3), w1.find(n4));
        t.checkExpect(w1.find(n1), w1.find(n2));
        t.checkExpect(w1.find(n2), w1.find(n3));
        t.checkExpect(w1.find(n3), w1.find(n4));
        t.checkExpect(w1.find(n4), w1.find(n1));

        w1.union(w1.find(n2), w1.find(n3));
        t.checkExpect(w1.find(n1), w1.find(n2));
        t.checkExpect(w1.find(n2), w1.find(n3));
        t.checkExpect(w1.find(n3), w1.find(n1));
        t.checkExpect(w1.find(n4), w1.find(n1));
    }

    // tests the has multiple trees method
    void testHasMultipleTrees(Tester t) {
        initMaze();
        Node n1 = w1.nodes.get(0).get(0);
        Node n2 = w1.nodes.get(0).get(1);
        Node n3 = w1.nodes.get(1).get(0);
        Node n4 = w1.nodes.get(1).get(1);

        t.checkExpect(w1.hasMultipleTrees(), true);
        w1.union(w1.find(n1), w1.find(n2));
        t.checkExpect(w1.hasMultipleTrees(), true);
        w1.union(w1.find(n2), w1.find(n3));
        t.checkExpect(w1.hasMultipleTrees(), true);
        w1.union(w1.find(n3), w1.find(n4));
        t.checkExpect(w1.hasMultipleTrees(), true);
    }

    // tests the make edges in tree method
    void testMakeEdgesInTree(Tester t) {
        initMaze();
        t.checkExpect(w1.edgesInTree.size(), 7);
        t.checkExpect(w2.edgesInTree.size(), 14);
    }

    // tests the make edges in tree method
    void testDrawNode(Tester t) {
        initMaze();
        t.checkExpect(a, a);
        t.checkExpect(b, b);
    }

    // tests the make edges in tree method
    void testMakeScene(Tester t) {
        initMaze();
        t.checkExpect(this.makeScene, makeScene);
    }

    // tests isEmpty
    void testIsEmpty(Tester t) {
        initMaze();
        t.checkExpect(this.exEdges.isEmpty(), true);
        this.exEdges.add(c);
        t.checkExpect(this.exEdges.isEmpty(), false);
    }

    // tests Remove
    void testRemove(Tester t) {
        initMaze();
        this.exEdges.add(e);
        t.checkExpect(this.exEdges.remove(0), e);
        this.exEdges.add(e);
        this.exEdges.add(c);
        t.checkExpect(this.exEdges.remove(1), c);
    }

    // tests Add
    boolean testAdd(Tester t) {
        return t.checkExpect(this.exEdges.add(e), true) && t.checkExpect(this.exEdges.add(c), true);
    }

    // tests Size
    void testSize(Tester t) {
        initMaze();
        t.checkExpect(this.exEdges.size(), 0);
    }

    // tests hashcode
    boolean testHashCode(Tester t) {
        return t.checkExpect(this.empty, null);
    }

    // tests that onKey returns a new board
    boolean testOnKeyEvent(Tester t) {
        initMaze();
        MazeWorld w3 = w1;
        w1.onKeyEvent("r");
        return !t.checkExpect(w1.worklist, w3.worklist)
                && !t.checkExpect(w1.edgesInTree, w3.edgesInTree);
    }

    // tests MazeWorld
    void testMazeWorld(Tester t) {
        MazeWorld background = new MazeWorld(20, 20, false);
        int sceneSize = 1000;
        background.bigBang(sceneSize, sceneSize, 0.001);
    }
}
