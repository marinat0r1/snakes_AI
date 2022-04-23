package m_hincu;

import snakes.Bot;
import snakes.Coordinate;
import snakes.Direction;
import snakes.Snake;

import java.util.Random;

public class MyBot implements Bot {

    private static final Direction[] DIRECTIONS = new Direction[] {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
    @Override
    public Direction chooseDirection(Snake snake, Snake opponent, Coordinate mazeSize, Coordinate apple) {
        Random random = new Random();
        Direction randomDirection = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        return randomDirection;
    }
}
