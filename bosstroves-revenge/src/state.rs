/// Main enum encompassing all of Bosstroves'
/// possible game states.
#[derive(Clone, PartialEq, Eq, Debug, Hash)]
pub enum GameState {
    /// The initial game state
    /// used for the logo screen.
    Loading,
    /// Game state used for full
    /// screen UIs, such as the
    /// title screen or settings
    /// screen.
    Ui,
    /// The main game state used
    /// whenever the overworld is
    /// playable.
    Active,
    /// Game state used for pausing
    /// in the overworld.
    Paused,
    /// Game state used when a
    /// transition animation is
    /// showing.
    Transitioning,
}
