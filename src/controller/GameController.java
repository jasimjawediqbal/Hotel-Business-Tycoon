package controller;

import model.*;
import view.*;
import javax.swing.*;

public class GameController {
    private GameState gameState;
    private GameView gameView;
    private StoryController storyController;

    public void showMainMenu() {
        SwingUtilities.invokeLater(() -> new MainMenuView(this));
    }

    public void startNewGame() {
        gameState = new GameState();
        storyController = new StoryController();
        SwingUtilities.invokeLater(() -> gameView = new GameView(this, gameState));
        storyController.showIntro();
    }

    public void endDay() {
        gameState.payStaffSalaries();
        gameState.nextDay();
        gameView.refresh(gameState);

        String forcedEnding = storyController.processDayEvents(gameState, gameView);

        if (forcedEnding != null) {
            new EndingController(gameState, forcedEnding);
            gameView.dispose();
            return;
        }

        if (gameState.isBankrupt()) {
            new EndingController(gameState, "Ruin");
            gameView.dispose();
            return;
        }

        if (gameState.day >= 14) {
            new EndingController(gameState, null);
            gameView.dispose();
        }
    }

    public void roomAction(Room room) {
        if (room.status == RoomStatus.BROKEN) {
            if (gameState.money < 50) {
                StoryDialog.showMessage("Not enough money to repair this room.");
                return;
            }
            gameState.money -= 50;
            room.status = RoomStatus.AVAILABLE;
            StoryDialog.showMessage("You repaired Room " + room.id + ".");
        } else if (room.status == RoomStatus.AVAILABLE) {
            room.status = RoomStatus.OCCUPIED;
            gameState.money += 40;
            gameState.reputation += 2;
            StoryDialog.showMessage("Room " + room.id + " is now occupied. Money earned!");
        } else if (room.status == RoomStatus.OCCUPIED) {
            room.status = RoomStatus.AVAILABLE;
        }
        storyController.onRoomInteraction(gameState, room);
        gameView.refresh(gameState);
    }

    public GameState getGameState() { return gameState; }
}
