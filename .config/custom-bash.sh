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
alias memhogs='ps auxf | sort -nr -k 4 | head -5'

# get top process eating cpu ##
alias cpuhogs='ps auxf | sort -nr -k 3 | head -5'

alias ls="ls --color=auto"
alias ef="fd . --exclude={.get,node_modules,tmp,target} --type f | fzf --header 'ENTER to edit' --reverse --preview='pistol {}' --bind 'ENTER:execute(emacsclient -t {})' --margin=3%"
alias ed="fd . --exclude={.get,node_modules,tmp,target} --type d | fzf --header 'ENTER to edit' --reverse --preview='pistol {}' --bind 'ENTER:execute(emacsclient -t {})' --margin=3%"

function fcd () {
cd $(find /home/pat -type d | fzf --header 'CD into' --reverse --preview='pistol {}' --margin=3%)
}

