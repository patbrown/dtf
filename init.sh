#!/bin/bash
eval `ssh-agent`
ssh-add ~/.ssh/rotating
source ~/.secrets/d23m
source ~/.salt
DOTFILES=.dtf
REMOTE=git@github.com:patbrown/dtf.git

alias dtf='/usr/bin/git --git-dir=$HOME/$DOTFILES/ --work-tree=$HOME'
git clone --bare $REMOTE $HOME/$DOTFILES
dtf checkout
