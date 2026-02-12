package controller;

import model.GameState;
import view.EndingView;

public class EndingController {

    public EndingController(GameState state, String forcedEnding) {
        // Determine the ending
        String ending = calculateEnding(state, forcedEnding);

        // Show ending view with stats
        new EndingView(ending, state);
    }

    private String calculateEnding(GameState s, String forced) {
        if (forced != null) return forced;

        // Determine ending based on story/game state
        if (s.soldHotel) return "Bittersweet Sale";
        if (s.ledgerInvestigated && s.ghostRoomSolved && s.treasureFound && s.reputation > 80)
            return "Golden Legacy";
        if (s.reputation > 50) return "Hard-Earned Success";

        return "Ruin";
    }
}
