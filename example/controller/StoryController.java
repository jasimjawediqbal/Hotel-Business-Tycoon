package controller;

import model.GameState;
import model.Room;
import model.RoomStatus;
import view.StoryDialog;

import javax.swing.*;
import java.awt.Component;

public class StoryController {
    private boolean chapter3Shown;
    private boolean chapter4Shown;
    private boolean chapter5Shown;
    private boolean chapter6Shown;
    private boolean chapter7Shown;

    public void showIntro() {
        StoryDialog.showMessage(
            "You inherited a ruined motel. Rebuild it, uncover the truth, and protect your legacy."
        );
    }

    public String processDayEvents(GameState state, Component parent) {
        if (!chapter3Shown && state.day >= 3) {
            chapter3Shown = true;
            int choice = JOptionPane.showConfirmDialog(
                parent,
                "You found an old ledger at reception. Investigate it?",
                "Chapter 3: The Old Register",
                JOptionPane.YES_NO_OPTION
            );
            state.ledgerInvestigated = choice == JOptionPane.YES_OPTION;
            if (state.ledgerInvestigated) {
                state.reputation += 5;
                StoryDialog.showMessage("You uncover hidden names and connections. Reputation +5.");
            } else {
                StoryDialog.showMessage("You ignore the ledger and focus on operations.");
            }
        }

        if (!chapter4Shown && state.day >= 5) {
            chapter4Shown = true;
            int choice = JOptionPane.showOptionDialog(
                parent,
                "Rashid is in debt. What do you do?",
                "Chapter 4: Rashid's Debt",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Pay Debt ($150)", "Negotiate", "Ignore"},
                "Pay Debt ($150)"
            );

            if (choice == 0 && state.money >= 150) {
                state.money -= 150;
                state.rashidDebtSolved = true;
                state.reputation += 10;
                StoryDialog.showMessage("You paid the debt. Staff loyalty and reputation improve.");
            } else if (choice == 1) {
                state.reputation += 2;
                StoryDialog.showMessage("Negotiation buys time, but risk remains.");
            } else {
                state.reputation -= 8;
                StoryDialog.showMessage("Ignoring the issue hurts morale and public image.");
            }
        }

        if (!chapter5Shown && state.day >= 7) {
            chapter5Shown = true;
            int choice = JOptionPane.showConfirmDialog(
                parent,
                "A rival offers to buy your hotel. Accept the sale?",
                "Chapter 5: Rival Offer",
                JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                state.soldHotel = true;
                return "Bittersweet Sale";
            }
            state.rivalDefeated = true;
            state.reputation += 5;
            StoryDialog.showMessage("You refuse the deal and keep fighting for the hotel.");
        }

        if (!chapter6Shown && state.day >= 9) {
            chapter6Shown = true;
            int choice = JOptionPane.showConfirmDialog(
                parent,
                "Guests report strange sounds from the ghost room. Investigate?",
                "Chapter 6: The Ghost Room",
                JOptionPane.YES_NO_OPTION
            );
            state.ghostRoomSolved = choice == JOptionPane.YES_OPTION;
            if (state.ghostRoomSolved) {
                state.reputation += 10;
                StoryDialog.showMessage("You uncover the room's truth. Reputation +10.");
            } else {
                StoryDialog.showMessage("You seal the room and avoid the mystery.");
            }
        }

        if (!chapter7Shown && state.day >= 11) {
            chapter7Shown = true;
            int choice = JOptionPane.showConfirmDialog(
                parent,
                "Search for your father's hidden backup assets?",
                "Chapter 7: Lost Treasure",
                JOptionPane.YES_NO_OPTION
            );
            state.treasureFound = choice == JOptionPane.YES_OPTION;
            if (state.treasureFound) {
                state.money += 250;
                state.reputation += 5;
                StoryDialog.showMessage("You found hidden assets. Money +250, Reputation +5.");
            } else {
                StoryDialog.showMessage("You skip the search and stay focused on daily operations.");
            }
        }

        return null;
    }

    public void onRoomInteraction(GameState state, Room room) {
        if (room.isGhostRoom && state.ghostRoomSolved && room.status == RoomStatus.OCCUPIED) {
            state.reputation += 2;
        }
    }
}
