public class Driver {
    public static void main(String[] args) {
        int xVal = Integer.parseInt(args[0]);
        int yVal = Integer.parseInt(args[1]);
        boolean breadthFirst = Boolean.parseBoolean(args[2]);

        MazeWorld maze = new MazeWorld(xVal, yVal, breadthFirst);

        int sceneSize = 1200;
        maze.bigBang(sceneSize, sceneSize, 0.001);
    }
}
