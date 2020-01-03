# Events
When players (or other entities) interact with tiles,
Bosstrove's Revenge runs event callbacks. Callbacks are 
specified in an object called `events`.

# Callbacks and Operations
Callbacks are generally labeled `on<Event>`. For example,

```
"events": {
  "onEntityStep": [
    {
      "operation": "set",
      "var": "%chaseFinishedYet",
      "value": "1"
    }
  ]
}
```

Callbacks are defined with an array of _operations_, which are
executed in the order that they are defined. Each operation is
specified as an object.

The key `operation` specifies the type
of the operation, and the other keys are inputs or outputs.
Both inputs or outputs can be variables in global, persistant,
or trigger data.

# API: Callbacks

### `onEntityHit`
- Run whenever an entity (or player) enters the hitbox of a tile.
- Before operations are run, a temporary triggerData variable named `side` is set to
  either `NORTH`, ` EAST`, `SOUTH`, or `WEST`. Like normal triggerData, it can be
  referenced in operations with `$side`.
- This corresponds to the `ai.arcblroth.boss.engine.IInteractable.onEntityHit()` method.

### `onEntityStep`
- Run whenever an entity (or player) is within the hitbox of a tile.
- This corresponds to the `ai.arcblroth.boss.engine.IInteractable.onEntityStep()` method.

### `onPlayerInteract`
- Run whenever a player is within the hitbox of a tile and enters a keybind.
- Before operations are run, a temporary triggerData variable named `keybind` is set to
  the id of the keybind that was pressed. Like normal triggerData, it can be
  referenced in operations with `$keybind`.
- This corresponds to the `ai.arcblroth.boss.engine.IInteractable.onPlayerInteract()` method.

# API: Operations

### `set`

Sets the variable `var` to the value/variable `value`.
- `var` - the variable to set.
- `value` - the value/variable to set `var` to.

### `math`

Sets the variable `var` to the result of a math statement.
- `lhs` defines the variable/value on the left of the statement.
- `rhs` defines the variable/value on the right of the statement.
- `type` defines the math operation to use. Valid types include `add`, `+`, `subtract`, `-`, `multiply`, `*`, `divide`, and `/`.
- If an exception is generated processing the statement, `var`'s value becomes undefined.

### `if`

Runs the operations in `run` if `lhs` `condition` `rhs` evaluates to true.
- `lhs` - Value/variable on the left of the if statement.
- `condition` - Either `==`, `!=`, `<`, `<=`, `>=`, or `>`
- `rhs` - Value/variable on the right of the if statement.
- `run` - An array of operations to run if the test succeeds. You can nest if statements in if statements, until of course a StackOverflow is generated...

### `trigger`

Triggers the cutscene `cutsceneId`.
- `cutsceneId` - id of cutscene to trigger.
