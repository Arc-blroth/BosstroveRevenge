package ai.arcblroth.boss.game.gui;

import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.gui.*;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.StaticDefaults;

public class HUD extends GUIParent {

	private Player player;
	private GUIPanel hpPanel;
	private GUIText hpText;
	private GUILevelBar hpBar;

	public HUD(Player player, GUILookAndFeel lookAndFeel) {
		this.player = player;
		this.hpPanel = new GUIPanel(lookAndFeel.panelBgColor, Color.TRANSPARENT, 0);
		this.hpText = new GUIText("HP ", Color.TRANSPARENT, lookAndFeel.textDeselectedFgColor);
		this.hpBar = new GUILevelBar(0.5, Color.RED, Color.GREEN);
		add(hpPanel, new GUIConstraints(0, 0, 1, 0, 0, 0, 0, 6, 0));
		hpPanel.add(hpText, new GUIConstraints(0, 0, 0, 0, 1, 2, 3, 2, 1));
		hpPanel.add(hpBar, new GUIConstraints(0, 0, 0.25, 0, 4, 2, 0, 2, 0));
	}

	@Override
	public void render(PixelAndTextGrid target) {
		hpBar.setLevel(player.getHealth() / StaticDefaults.MAX_PLAYER_HEALTH);
		super.render(target);
	}

}
