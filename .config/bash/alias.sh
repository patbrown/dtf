alias h='hstr'
alias dtf='/usr/bin/git --git-dir=/home/pat/dtf/ --work-tree=/home/pat'
alias di='export ISS=$(gh issue list | fzf | cut -d$'"'"'\t'"'"' -f1) && gh issue develop $ISS -b canary --checkout'
### ALIAS
alias sz='source ~/.zshrc'
alias sync-shell='cd ~/hfc && git pull origin main && popd'
alias di='export ISS=$(gh issue list | fzf | cut -d$'"'"'\t'"'"' -f1) && gh issue develop $ISS -b canary --checkout'

# Colorize grep output (good for log files)
alias grep='grep --color=auto'
alias egrep='egrep --color=auto'
alias fgrep='fgrep --color=auto'

# confirm before overwriting something
alias cp="cp -i"
alias mv='mv -i'
alias rm='rm -i'

# easier to read disk
alias df='df -h'     # human-readable sizes
alias free='free -m' # show sizes in MB

# get top process eating memory
alias psmem='ps auxf | sort -nr -k 4 | head -5'

# get top process eating cpu ##
alias pscpu='ps auxf | sort -nr -k 3 | head -5'

export GPGUSER="Pat Brown"
export EMAIL="pat@drilling.net"
export EDITOR="ec"
export FZF_DEFAULT_OPTS="--extended"
export ZSH_DISABLE_COMPFIX=true
export CLICOLOR=1
export LSCOLORS=ExGxBxDxCxEgEdxbxgxcxd
export EA_EDITOR='emacsclient -a "" -c'
export GRAALVM_HOME=$HOME/bin/graalvm-ce-java11-22.3.1
export FZF_DEFAULT_COMMAND='fd --type f --hidden'
export FZF_DEFAULT_OPTS=
export FZF_CTRL_T_COMMAND="$FZF_DEFAULT_COMMAND"
export FZF_CTRL_T_OPTS="--header 'ENTER to edit' --reverse --preview='pistol {}' --bind 'enter:execute(emacsclient -t {})' --margin=3%"
export FZF_ALT_C_COMMAND='fd --type d . --color=never --hidden'
export FZF_ALT_C_OPTS="--preview 'tree -C {} | head -50'"
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"  # This loads nvm bash_completion
function fj { bb cmd $@;}

function bbe () {
bb -e "$1"
}

function bbf () {
bb -f "$1"
}

function run-main () {
clojure -M -m "$1"
}
alias ef="fd . --exclude={.get,node_modules,tmp,target} --type f | fzf --header 'ENTER to edit' --reverse --preview='pistol {}' --bind 'ENTER:execute(emacsclient -t {})' --margin=3%"
alias ed="fd . --exclude={.get,node_modules,tmp,target} --type d | fzf --header 'ENTER to edit' --reverse --preview='pistol {}' --bind 'ENTER:execute(emacsclient -t {})' --margin=3%"

# function ed () {
# find /home/pat ! -name '*.class' ! -path '*.git*' -type d | fzf --header 'ENTER to edit' --reverse --preview='pistol {}' --bind 'ENTER:execute(emacsclient -t {})' --margin=3%
# }



_bb_tasks() {
    local matches=(`bb tasks |tail -n +3 |cut -f1 -d ' '`)
    compadd -a matches
    _files # autocomplete filenames as well
}
function title {
    echo -ne "\033]0;"$*"\007"
}

export PATH=/usr/local/bin:$PATH
export PATH="$HOME/bin:$PATH"

if ! shopt -oq posix; then
  if [ -f /usr/share/bash-completion/bash_completion ]; then
    . /usr/share/bash-completion/bash_completion
  elif [ -f /etc/bash_completion ]; then
    . /etc/bash_completion
  fi
fi

bind TAB:menu-complete
