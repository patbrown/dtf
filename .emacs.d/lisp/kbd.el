(global-set-key (kbd "C-v") 'backward-char)
(global-set-key (kbd "<escape>") 'keyboard-escape-quit)
(global-set-key     (kbd "C-c C-c a") 'append-to-register)
(global-set-key     (kbd "C-c C-c i") 'insert-register)
(global-set-key     (kbd "C-c C-c w") 'window-configuration-to-register)
(global-set-key     (kbd "C-c C-c y") 'copy-to-register)
(global-set-key     (kbd "C-x r l")   'list-register)
(global-set-key     (kbd "M-h")               'fzf)
(global-set-key     (kbd "C-i")               'completion-at-point)
(global-set-key     (kbd "C-x C-r")               'counsel-register)
(global-set-key     (kbd "C-c C-x r")         'eval-region)
(global-set-key     (kbd "C-c C-x b")         'eval-buffer)
(global-set-key     (kbd "RET")               'newline-and-indent)
(global-set-key     (kbd "C-j")               'newline-and-indent)
(global-set-key     (kbd "C-l")               'goto-line)
(global-set-key     (kbd "M-;")               'comment-dwim)
(global-set-key     (kbd "C-c a")               'beginning-of-buffer)
(global-set-key (kbd "C-t") 'forward-paragraph)
(global-set-key (kbd "C-r") 'backward-paragraph)
(global-set-key     (kbd "C-c e")               'end-of-buffer)
(global-set-key     (kbd "C-h C-f")           'find-function)
(global-set-key     (kbd "C-h C-k")           'find-function-on-key)
(global-set-key     (kbd "C-h C-v")           'find-variable)
(global-set-key     (kbd "C-h C-l")           'find-library)
(global-set-key     (kbd "C-SPC")             'set-mark-command)
(global-set-key (kbd "C-x |")                 'toggle-window-split)
(global-set-key (kbd "C-c w")                 'rotate-windows)
(global-set-key [remap move-beginning-of-line]
                'smarter-move-beginning-of-line)
(global-set-key (kbd "C-c C-w")               'kill-ring-save)
(global-set-key (kbd "C-c C-p")               'ace-window)
(global-set-key (kbd "S-C-<right>")           'paredit-forward-slurp-sexp)
(global-set-key (kbd "S-C-<left>")            'paredit-backward-slurp-sexp)
(global-set-key (kbd "S-C-<up>")              'paredit-backward)
(global-set-key (kbd "S-C-<down>")            'paredit-forward)
(global-set-key (kbd "C-c C-j")               'jump-to-register)
(global-set-key (kbd "C-c C-x C-k")           'browse-kill-ring)
(global-set-key (kbd "M-x")                   'counsel-M-x)
;(global-set-key (kbd "C-c g")                 'counsel-git)
(global-set-key (kbd "C-c G")                 'counsel-git-grep)
(global-set-key (kbd "C-x b") 'ivy-switch-buffer)
(global-set-key (kbd "C-x f") 'projectile-find-file)
;; YASNIPPETS
(global-set-key (kbd "C-x C-f") 'find-file)
(global-set-key (kbd "C-c C-f") 'projectile-find-file-dwim)
(global-set-key (kbd "C-c u") 'swiper-all)
(global-set-key (kbd "C-x C-d") 'dirvish-dired)
(global-set-key (kbd "C-c C-f") 'counsel-projectile-find-file)
(global-set-key (kbd "C-x <") 'previous-buffer)
(global-set-key (kbd "C-x >") 'next-buffer)
(global-set-key (kbd "C-c r") 'cider-eval-last-sexp-in-buffer-as-current-region)
(global-set-key (kbd "C-c s") 'cider-eval-sexp-at-point)
(global-set-key (kbd "C-c t") 'cider-tap-last-expression)
(global-set-key (kbd "C-c <") 'lsp-ui-find-prev-reference)
(global-set-key (kbd "C-c d") 'lsp-find-definition)
(global-set-key (kbd "C-c C-c r") 'lsp-find-references)
(global-set-key (kbd "C-<up>") 'windmove-up)
(global-set-key (kbd "C-<down>") 'windmove-down)
(global-set-key (kbd "C-c v") 'indent-sexp)
(global-set-key (kbd "M-u") 'delete-indentation)
(global-set-key (kbd "M-U") 'indent-region)

;; # KEYMAP SYSTEM
(global-unset-key (kbd "C-c c"))
(define-prefix-command 'start-map)
(global-set-key (kbd "C-c c") 'start-map)
(define-key start-map (kbd "+") 'help)
(global-unset-key (kbd "C-c c"))
(define-prefix-command 'start-map)
(define-prefix-command 'clj-map)
(define-prefix-command 'lisp-map)
(define-prefix-command 'jump-map)
(define-prefix-command 'tab-map)
(define-prefix-command 'z-map)
(define-prefix-command 'o-map)
(define-prefix-command 'v-map)
(global-set-key (kbd "C-c c") 'start-map)
(define-key start-map (kbd "b") 'bufler-list)
(define-key start-map (kbd "c") 'clj-map)
(define-key start-map (kbd "e") 'eval-buffer)
(define-key start-map (kbd "a") 'winner-undo)
(define-key start-map (kbd "s") 'winner-redo)
(define-key start-map (kbd "d") 'windmove-left)
(define-key start-map (kbd "f") 'windmove-right)
(define-key start-map (kbd "j") 'jump-map)
(define-key start-map (kbd "l") 'lisp-map)
(define-key start-map (kbd "n") 'tab-line-switch-to-next-tab)
(define-key start-map (kbd "p") 'tab-line-switch-to-prev-tab)
(define-key start-map (kbd "u") 'swiper-all)
(define-key start-map (kbd "w") 'burly-open-bookmark)
(define-key start-map (kbd "x") 'delete-indentation)
(define-key start-map (kbd "z") 'z-map)


;; Jump Map
(global-set-key (kbd "C-c j") 'jump-map)
(define-key jump-map (kbd "a") 'beginning-of-buffer)
(define-key jump-map (kbd "b") 'burly-open-bookmark)
(define-key jump-map (kbd "e") 'end-of-buffer)
(define-key jump-map (kbd "f") 'ace-window)
(define-key jump-map (kbd "c") 'ace-jump-char-mode)
(define-key jump-map (kbd "l") 'ace-jump-line-mode)
(define-key jump-map (kbd "n") 'tab-line-switch-to-next-tab)
(define-key jump-map (kbd "p") 'tab-line-switch-to-prev-tab)
(define-key jump-map (kbd "j") 'jump-to-register)
(define-key jump-map (kbd "w") 'ace-jump-word-mode)
(define-key jump-map (kbd "x") 'tab-line-close-tab)
(define-key jump-map (kbd "z") 'ace-jump-zap-up-to-char)

;; Lisp Map mostly paredit
(global-set-key (kbd "C-c l") 'lisp-map)
(define-key lisp-map (kbd ";") 'paredit-forward-slurp-sexp)
(define-key lisp-map (kbd "l") 'paredit-backward-slurp-sexp)
(define-key lisp-map (kbd "k") 'paredit-forward-barf-sexp)
(define-key lisp-map (kbd "j") 'paredit-backward-barf-sexp)
(define-key lisp-map (kbd "r") 'paredit-wrap-round)
(define-key lisp-map (kbd "c") 'paredit-wrap-curly)
(define-key lisp-map (kbd "s") 'paredit-wrap-square)
    
(global-unset-key (kbd "<left>"))
(global-unset-key (kbd "<right>"))
(global-unset-key (kbd "<up>"))
(global-unset-key (kbd "<down>"))
;(global-unset-key (kbd "C-o"))
(global-unset-key (kbd "C-v"))
(global-unset-key (kbd "M-s"))
(global-set-key (kbd "C-c f s") #'vimish-fold)
  (global-set-key (kbd "C-c f d") #'vimish-fold-delete)
  (global-set-key (kbd "C-c f a") #'vimish-fold-unfold-all)
  (global-set-key (kbd "C-c f A") #'vimish-fold-refold-all)
  (global-set-key (kbd "C-c f f") #'vimish-fold-toggle)
  (global-set-key (kbd "C-c f F") #'vimish-fold-toggle-all)
  (global-set-key (kbd "C-c <left>") 'windmove-left)
  (global-set-key (kbd "C-c <right>") 'windmove-right)
  (global-set-key (kbd "C-c <up>")   'windmove-up)
  (global-set-key (kbd "C-c <down>") 'windmove-down)

