# Set a custom session root path. Default is `$HOME`.
# Must be called before `initialize_session`.
session_root "~/fjck"

# Create session with specified name if it does not already exist. If no
# argument is given, session name will be based on layout file name.
if initialize_session "test-session"; then

  # Load a defined window layout.
  load_window "test-window"

  # Select the default active window on session creation.
  select_window 1
  run_cmd "ls"
  
fi

# Finalize session creation and switch/attach to it.
finalize_and_go_to_session
