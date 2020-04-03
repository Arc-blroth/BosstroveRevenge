package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.engine.gui.GUIPanel;
import ai.arcblroth.boss.engine.gui.GUIText;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.TextureUtils;

import java.util.ArrayList;
import java.util.Objects;

public class AnimatedGUITextPanel extends GUIPanel implements IAdvanceableDialog {

	private ArrayList<Runnable> callbacks = new ArrayList<>();
	private Color textBgColor;
	private Color textFgColor;
	private String text;
	private boolean wordWrap;
	private int frame;
	private float animationSpeed;

	public AnimatedGUITextPanel() {
		super();
		this.textBgColor = Color.TRANSPARENT;
		this.textFgColor = Color.BLACK;
		this.text = "";
		this.frame = 0;
		this.animationSpeed = 0;
	}

	/**
	 * Creates a new AnimatedGUITextPanel with word wrapping enabled and animation disabled.
	 * @param backgroundColor background color of this panel
	 * @param borderColor border color of this panel
	 * @param textBackgroundColor background color of text
	 * @param textForegroundColor foreground color of text
	 * @param borderWidth width of border in pixels
	 * @param text panel text.
	 */
	public AnimatedGUITextPanel(Color backgroundColor, Color borderColor, Color textBackgroundColor, Color textForegroundColor, int borderWidth, String text) {
		this(backgroundColor, borderColor, textBackgroundColor, textForegroundColor, borderWidth, text, 0);
	}

	/**
	 * Creates a new AnimatedGUITextPanel with word wrapping enabled.
	 * @param backgroundColor background color of this panel
	 * @param borderColor border color of this panel
	 * @param textBackgroundColor background color of text
	 * @param textForegroundColor foreground color of text
	 * @param borderWidth width of border in pixels
	 * @param text panel text.
	 * @param animationSpeed animation speed in characters advanced per frame. Set to 0 to disable animation.
	 */
	public AnimatedGUITextPanel(Color backgroundColor, Color borderColor, Color textBackgroundColor, Color textForegroundColor, int borderWidth, String text, float animationSpeed) {
		this(backgroundColor, borderColor, textBackgroundColor, textForegroundColor, borderWidth, text, true, animationSpeed);
	}

	/**
	 * Creates a new AnimatedGUITextPanel.
	 * @param backgroundColor background color of this panel
	 * @param borderColor border color of this panel
	 * @param textBackgroundColor background color of text
	 * @param textForegroundColor foreground color of text
	 * @param borderWidth width of border in pixels
	 * @param text panel text.
	 * @param wordWrap if true, panel text will be wrapped based on word boundaries; if false, panel text will be wrapped based on character boundaries.
	 * @param animationSpeed animation speed in characters advanced per frame. Set to 0 to disable animation.
	 */
	public AnimatedGUITextPanel(Color backgroundColor, Color borderColor, Color textBackgroundColor, Color textForegroundColor, int borderWidth, String text, boolean wordWrap, float animationSpeed) {
		super(backgroundColor, borderColor, borderWidth);
		this.textBgColor = textBackgroundColor;
		this.textFgColor = textForegroundColor;
		this.text = text;
		this.wordWrap = wordWrap;
		this.frame = 0;
		this.animationSpeed = animationSpeed;
	}

	@Override
	public synchronized void render(PixelAndTextGrid target) {
		super.render(target);
		if(text != null && !text.equals("")) {
			if(target.getWidth() - 2 * super.getBorderWidth() - 2 > 0 && target.getHeight() - 2 * super.getBorderWidth() - 2 > 0) {
				PixelAndTextGrid nonborderedTarget = TextureUtils.buildTransparentTextGrid(target.getWidth() - 2 * super.getBorderWidth(), target.getHeight() - 2 * super.getBorderWidth());
				String displayedText;
				if(animationSpeed <= 0) {
					displayedText = text;
				} else {
					displayedText = text.substring(0, (int)Math.floor(Math.max(Math.min(frame * animationSpeed, text.length()), 0)));
				}
				GUIText guiText = new GUIText(displayedText, textBgColor, textFgColor, wordWrap);
				guiText.render(nonborderedTarget);
				TextureUtils.overlay(nonborderedTarget, target, super.getBorderWidth() + 1, super.getBorderWidth() + 2);
			}
		}
	}

	public synchronized void advanceFrame() {
		if(canAdvanceFrame()) frame += animationSpeed;
	}

	public synchronized boolean canAdvanceFrame() {
		return (int)Math.floor((frame - 1) * animationSpeed) < text.length();
	}

	@Override
	public void onInput(Keybind k) {
		super.onInput(k);
		if(!canAdvanceFrame()) {
			if (k.equals(new Keybind("boss.use")) || k.equals(new Keybind("boss.enter"))) {
				callbacks.forEach(Runnable::run);
			}
		}
	}

	@Override
	public void onAdvance(Runnable callback) {
		callbacks.add(Objects.requireNonNull(callback));
	}
}
