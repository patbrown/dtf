source "${HOME}/.zgen/zgen.zsh"

if ! zgen saved; then

  zgen oh-my-zsh
  zgen oh-my-zsh plugins/git
  zgen load spaceship-prompt/spaceship-prompt spaceship
  zgen load zsh-users/zsh-syntax-highlighting
  zgen load zsh-users/zsh-autosuggestions
  zgen load zsh-users/zsh-completions
  zgen load zsh-users/zsh-history-substring-search
  zgen load hlissner/zsh-autopair
  
  zgen save
fi

autopair-init
source ~/.config/zsh/config.zsh
source ~/.salt
