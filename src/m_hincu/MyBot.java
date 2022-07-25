package m_hincu;

import snakes.Bot;
import snakes.Coordinate;
import snakes.Direction;
import snakes.Snake;

import java.util.*;

/**
 * This class represents my implementation for the optimal bot to win the snake 2v2 game.
 *
 * @author Marin-Petru Hincu
 */
public class MyBot implements Bot {

    private final Random rnd = new Random();
    private static final Direction[] DIRECTIONS = new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

    /**
     * This Method contains all the main functionality, which decides which direction is chosen.
     * It is called with each step the snake has to take and must return a direction which the snake will then follow.
     *
     * @param snake    Your snake's body with coordinates for each segment
     * @param opponent Opponent snake's body with coordinates for each segment
     * @param mazeSize Size of the board
     * @param apple    Coordinate of an apple
     * @return The direction which the snake will follow.
     *
     */
    @Override
    public Direction chooseDirection(Snake snake, Snake opponent, Coordinate mazeSize, Coordinate apple) {
        Coordinate head = snake.getHead();
        Coordinate headOpponent = opponent.getHead();

        Coordinate afterHeadNotFinal = null;
        if (snake.body.size() >= 2) {
            Iterator<Coordinate> it = snake.body.iterator();
            it.next();
            afterHeadNotFinal = it.next();
        }

        final Coordinate afterHead = afterHeadNotFinal;
        /* Directions are filtered, it is checked that the backwards direction is excluded */
        Direction[] validMoves = Arrays.stream(DIRECTIONS)
                .filter(d -> !head.moveTo(d).equals(afterHead))
                .sorted()
                .toArray(Direction[]::new);

        Coordinate afterHeadNotFinalOp = null;
        if (opponent.body.size() >= 2) {
            Iterator<Coordinate> it = opponent.body.iterator();
            it.next();
            afterHeadNotFinalOp = it.next();
        }

        final Coordinate afterHeadOp = afterHeadNotFinalOp;
        Direction[] validMovesOp = Arrays.stream(DIRECTIONS)
                .filter(d -> !headOpponent.moveTo(d).equals(afterHeadOp))
                .sorted()
                .toArray(Direction[]::new);

        /* Filtering out all directions that would lead to a loose */
        Direction[] notLosing = Arrays.stream(validMoves)
                .filter(d -> head.moveTo(d).inBounds(mazeSize))             // Don't leave maze
                .filter(d -> !opponent.elements.contains(head.moveTo(d)))   // Don't collide with opponent
                .filter(d -> !snake.elements.contains(head.moveTo(d)))      // Don't collide with yourself
                .sorted()
                .toArray(Direction[]::new);


        if (snake.elements.size() <= 7) {
            return stageOneDirection(snake, opponent, mazeSize, apple, head, headOpponent, validMoves, validMovesOp, notLosing);
        } else if (snake.elements.size() > 7 && snake.elements.size() < 15) {
            return stageTwoDirection(snake, opponent, mazeSize, apple, head, headOpponent, validMoves, validMovesOp, notLosing);
        } else {
            return stageThreeDirection(snake, opponent, mazeSize, apple, head, headOpponent, validMoves, validMovesOp, notLosing);
        }

    }


    /**
     * Here we decide whether the most optimal move is to go for the apple, or if it is better to circle around the middle.
     * Whenever our snake has the chance to reach the apple and is closer to the apple than the opponent, it will go for the apple.
     * If our snake has no possibility to reach the apple, or the opponent snake is closer to the apple,
     * our snake will circle towards the maze midpoint to have a better position for the next apple.
     *
     * @param snake
     * @param opponent
     * @param mazeSize
     * @param apple
     * @param head
     * @param headOpponent
     * @param validMoves
     * @param validMovesOp
     * @param notLosing
     * @return
     */

    private Direction stageOneDirection(Snake snake, Snake opponent, Coordinate mazeSize, Coordinate apple, Coordinate head, Coordinate headOpponent, Direction[] validMoves, Direction[] validMovesOp, Direction[] notLosing) {
        if (notLosing.length > 0) {
            double shortestDistanceToAppleOpponent = calculateManhattanDistance(headOpponent, apple);
            Tuple2<Direction, Double> toApple = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, apple);
            if (shortestDistanceToAppleOpponent > toApple.getSecond() && toApple.getFirst() != null) {
                return toApple.getFirst();
            } else {
                Tuple2<Direction, Double> toMazeMidPoint = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(7,7));
                return toMazeMidPoint.getFirst();
            }
        } else {
            return validMoves[0];
        }
    }

    /**
     *
     * @param snake
     * @param opponent
     * @param mazeSize
     * @param apple
     * @param head
     * @param headOpponent
     * @param validMoves
     * @param validMovesOp
     * @param notLosing
     * @return
     */
    private Direction stageTwoDirection(Snake snake, Snake opponent, Coordinate mazeSize, Coordinate apple, Coordinate head, Coordinate headOpponent, Direction[] validMoves, Direction[] validMovesOp, Direction[] notLosing) {
        if (notLosing.length > 0) {
            double shortestDistanceToAppleOpponent = calculateManhattanDistance(headOpponent, apple);
            Tuple2<Direction, Double> toApple = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, apple);
            if (shortestDistanceToAppleOpponent > toApple.getSecond() && toApple.getFirst() != null) {
                return toApple.getFirst();
            } else {
                Tuple2<Direction, Double> toUpperLeftCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(4,4));
                Tuple2<Direction, Double> toLowerLeftCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(4,10));
                Tuple2<Direction, Double> toUpperRightCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(10,4));
                Tuple2<Direction, Double> toLowerRightCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(10,10));

                if (toUpperLeftCorner.getSecond() < toLowerLeftCorner.getSecond() && toUpperLeftCorner.getSecond() < toUpperRightCorner.getSecond() && toUpperLeftCorner.getSecond() < toLowerRightCorner.getSecond()){
                    return toUpperLeftCorner.getFirst();
                } else if (toLowerLeftCorner.getSecond() < toUpperLeftCorner.getSecond() && toLowerLeftCorner.getSecond() < toUpperRightCorner.getSecond() && toLowerLeftCorner.getSecond() < toLowerRightCorner.getSecond()){
                    return toLowerLeftCorner.getFirst();
                } else if (toUpperRightCorner.getSecond() < toUpperLeftCorner.getSecond() && toUpperRightCorner.getSecond() < toLowerLeftCorner.getSecond() && toUpperRightCorner.getSecond() < toLowerRightCorner.getSecond()){
                    return toUpperRightCorner.getFirst();
                } else if (toLowerRightCorner.getSecond() < toUpperLeftCorner.getSecond() && toLowerRightCorner.getSecond() < toLowerLeftCorner.getSecond() && toLowerRightCorner.getSecond() < toUpperRightCorner.getSecond()){
                    return toLowerRightCorner.getFirst();
                }
                return toUpperLeftCorner.getFirst();
            }
        } else {
            return validMoves[0];
        }
    }

    /**
     *
     * @param snake
     * @param opponent
     * @param mazeSize
     * @param apple
     * @param head
     * @param headOpponent
     * @param validMoves
     * @param validMovesOp
     * @param notLosing
     * @return
     */
    private Direction stageThreeDirection(Snake snake, Snake opponent, Coordinate mazeSize, Coordinate apple, Coordinate head, Coordinate headOpponent, Direction[] validMoves, Direction[] validMovesOp, Direction[] notLosing) {
        if (notLosing.length > 0) {
            double shortestDistanceToAppleOpponent = calculateManhattanDistance(headOpponent, apple);
            Tuple2<Direction, Double> toApple = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, apple);
            if (shortestDistanceToAppleOpponent > toApple.getSecond() && toApple.getFirst() != null) {
                return toApple.getFirst();
            } else {
                Tuple2<Direction, Double> toUpperLeftCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(0,0));
                Tuple2<Direction, Double> toLowerLeftCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(0,14));
                Tuple2<Direction, Double> toUpperRightCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(14,0));
                Tuple2<Direction, Double> toLowerRightCorner = calculateGivenDirection(mazeSize, notLosing, validMovesOp, head, snake, opponent, new Coordinate(14,14));

                if (toUpperLeftCorner.getSecond() < toLowerLeftCorner.getSecond() && toUpperLeftCorner.getSecond() < toUpperRightCorner.getSecond() && toUpperLeftCorner.getSecond() < toLowerRightCorner.getSecond()){
                    return toUpperLeftCorner.getFirst();
                } else if (toLowerLeftCorner.getSecond() < toUpperLeftCorner.getSecond() && toLowerLeftCorner.getSecond() < toUpperRightCorner.getSecond() && toLowerLeftCorner.getSecond() < toLowerRightCorner.getSecond()){
                    return toLowerLeftCorner.getFirst();
                } else if (toUpperRightCorner.getSecond() < toUpperLeftCorner.getSecond() && toUpperRightCorner.getSecond() < toLowerLeftCorner.getSecond() && toUpperRightCorner.getSecond() < toLowerRightCorner.getSecond()){
                    return toUpperRightCorner.getFirst();
                } else if (toLowerRightCorner.getSecond() < toUpperLeftCorner.getSecond() && toLowerRightCorner.getSecond() < toLowerLeftCorner.getSecond() && toLowerRightCorner.getSecond() < toUpperRightCorner.getSecond()){
                    return toLowerRightCorner.getFirst();
                }
                return toUpperLeftCorner.getFirst();
            }
        } else {
            return validMoves[0];
        }
    }

    /**
     * The manhattan distance between two coordinates is calculated and returned.
     *
     * @param a The coordinate of the starting point
     * @param b The coordinate of the destination
     * @return The manhattan distance is returned
     */
    private double calculateManhattanDistance(Coordinate a, Coordinate b) {
        return Math.sqrt(Math.abs(a.x - b.x) + Math.abs(a.y - b.y));
    }


    /**
     * The shortest Direction to a given destination is calculated here as well as the shortest distance to this destination.
     * The constraints of the game field as well as the position of the opponent snake is taken into account.
     * The returned values can be used to further evaluate which move is the most optional in a given situation.
     *
     * @param mazeSize The size of the field - it has to be checked for the snake to not leave the field
     * @param notLosing An array of directions containing all viable directions
     * @param validMovesOp An array with all possible directions that don't leave the maze
     * @param head The coordinate of our snakes head
     * @param snake Our snake
     * @param opponent The opponents snake
     * @param destination The destination coordinate our snake wants to reach
     * @return A 2D Tuple which returns the shortest distance as well as the optimal direction to reach the destination is returned
     */
    private Tuple2<Direction, Double> calculateGivenDirection(Coordinate mazeSize, Direction[] notLosing, Direction[] validMovesOp, Coordinate head, Snake snake, Snake opponent, Coordinate destination) {
        double shortestDistanceToDestination = Math.max(mazeSize.x, mazeSize.y) + 1;
        Direction shortestDirectionToMazeDestination = null;

        for (Direction dir : notLosing) {
            double dist = calculateManhattanDistance(head.moveTo(dir), destination);

            Snake new_snake = snake.clone();
            new_snake.moveTo(dir, false);

            boolean result = true;
            for (Direction dOp : validMovesOp) {
                Snake new_opponent = opponent.clone();
                new_opponent.moveTo(dOp, false);

                result = result & !new_opponent.elements.contains(new_snake.getHead());
            }

            if (dist < shortestDistanceToDestination && result) {
                shortestDistanceToDestination = dist;
                shortestDirectionToMazeDestination = dir;
            }
        }
        return new Tuple2<>(shortestDirectionToMazeDestination, shortestDistanceToDestination);
    }

}

